package com.zspt.blibli.main.server.impl;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zspt.blibli.account.controller.vo.UserInfoVo;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.account.server.impl.UserServerImpl;
import com.zspt.blibli.common.utils.FileUtils;
import com.zspt.blibli.common.utils.RedisUtils;
import com.zspt.blibli.common.utils.SystemConstants;
import com.zspt.blibli.common.vo.PageResult;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.config.FilePathConfig;
import com.zspt.blibli.main.controller.requestParam.ReplyParam;
import com.zspt.blibli.main.controller.requestParam.VideosParam;
import com.zspt.blibli.main.controller.vo.*;
import com.zspt.blibli.main.controller.vo.comment.CommentListVo;
import com.zspt.blibli.main.controller.vo.comment.CommentUserInfo;
import com.zspt.blibli.main.controller.vo.comment.Reply;
import com.zspt.blibli.main.dto.VideosDTO;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.*;
import com.zspt.blibli.main.mapper.domin.*;
import com.zspt.blibli.main.server.VideosServer;
import com.zspt.blibli.main.server.dao.video.VideoCacheDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideosServerImpl extends ServiceImpl<VideosMapper, Videos> implements VideosServer {

    @Resource
    private CategoriesServerImpl categoryServerImpl;

    @Resource
    private FilePathConfig filePathConfig;

    @Resource
    private FileServerImpl fileServerImpl;

    @Resource
    private UserServerImpl userServerImpl;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private VideoCoinMapper videoCoinMapper;

    @Resource
    private VideoCollectMapper videoCollectMapper;

    @Resource
    private VideoLikeMapper videoLikeMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private CommentMapper commentMapper;


    @Resource
    private CommentReplyMapper commentReplyMapper;

    @Override
    public Result addVideos(VideosParam videosParam) throws IOException {
        long currentUserId = StpUtil.getLoginIdAsLong();
        String fileName = FileUtils.generateFinalFilename(videosParam.getCover().getOriginalFilename());
        Path filePath = Paths.get(filePathConfig.getCoverPath(), fileName);
        // 保存封面文件
            videosParam.getCover().transferTo(filePath.toFile());
            VideosDTO videosDTO=new VideosDTO();

            BeanUtils.copyProperties(videosParam,videosDTO);
            if(videosDTO.getUserId()!=null)throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
            videosDTO.setCoverUrl(fileName);
            videosDTO.setUserId(currentUserId);

            Videos videos=new Videos();
            BeanUtils.copyProperties(videosDTO,videos);

            log.info("视频添加: {}", videos);
            boolean is = save(videos);
            if (!is) throw new Appexception(AppExceptionCodeMsg.VIDEO_UPLOAD_FAILED);
//            进行视频合并
          SystemConstants.CACHE_REBuILD_EXECUTOR.submit(()->{
              try {
                  fileServerImpl.mergeChunks(videosParam.getUid(),videos.getVideoId());
                }catch (Exception e){
                throw  new RuntimeException(e);
            }
            });
            return  Result.success(null);
        }

    @Override
    public Result getCategories() {
        List<CategoryVo> list = categoryServerImpl.lambdaQuery().eq(Categories::getParentId, 0).list().stream().map(categories -> {
            CategoryVo categoryVo = new CategoryVo();
            BeanUtils.copyProperties(categories, categoryVo);
            return categoryVo;
        }).collect(Collectors.toList());
        return   Result.success(list);
    }

    /**
     * 获取视频封面
     * @param couverUrl
     * @return
     */
    @Override
    public  ResponseEntity<FileSystemResource> getCover(String couverUrl) throws IOException {
        Path cover = FileUtils.buildSafePath(filePathConfig.getCoverPath(), couverUrl);
        if (!Files.exists(cover)) throw  new Appexception(AppExceptionCodeMsg.VIDEO_COVER_NOT_FOUND);
        File file=cover.toFile();
        FileSystemResource resource =  new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(cover))
                .body(resource);
    }

    /**
     * 推荐视频
     * @return
     */
    @Override
    public Result<List<VideoList>> getRecommend() {
        // 1. 优先从缓存获取数据
        String videoJson = stringRedisTemplate.opsForValue().get(SystemConstants.REDIS_VIDEO_RECOMMEND);

        List<VideoList> resultList;

        // 2. 如果缓存存在，直接反序列化为对象列表
        if (videoJson != null) {
            resultList = JSONUtil.toList(videoJson, VideoList.class);
        }
        // 3. 如果缓存不存在，查询数据库并更新缓存
        else {
            resultList = baseMapper.recommend();

            // 避免缓存穿透：即使结果为空也缓存空列表
            String jsonStr = JSONUtil.toJsonStr(resultList);
            stringRedisTemplate.opsForValue().set(
                    SystemConstants.REDIS_VIDEO_RECOMMEND,
                    jsonStr,
                    3, // 缓存有效期
                    TimeUnit.SECONDS
            );
        }

        // 4. 使用 Optional 确保返回非空列表
        return Result.success(
                Optional.ofNullable(resultList)
                        .orElse(Collections.emptyList())
        );
    }

    /**
     * 返回m3u8文件
     * @param m3u8Url
     * @return
     */
    @Override
    public ResponseEntity<FileSystemResource> getM3u8(String m3u8Url) {
        // 1. 构建安全路径（防止路径遍历攻击）
        Path video = FileUtils.buildSafePath(filePathConfig.getVideoPath(), m3u8Url);
        // 2. 检查文件是否存在
        if (!Files.exists(video)) {
            throw new Appexception(AppExceptionCodeMsg.FILE_NOT_FOUND);
        }

        Path resolve = video.resolve("index.m3u8");
        // 3. 准备文件资源w
        File file = resolve.toFile();
        FileSystemResource resource = new FileSystemResource(file);
        // 4. 设置HTTP响应头
        System.out.println("1111111111111");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.apple.mpegurl")); // M3U8标准MIME类型
        headers.setContentLength(file.length());
        headers.set("Accept-Ranges", "bytes"); // 支持断点续传

        // 5. 返回带有资源的响应实体
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }


    @Override
    public Result getVideoInfo(Long videId) {
        //        登陆用户id
        long loginIdAsLong = StpUtil.getLoginIdAsLong();

        VideoInfo videoCache = getVideoCache(videId,loginIdAsLong);
//        缓存拼接
        if(videoCache!=null){
            return Result.success(videoCache);
        }
        //获取视频信息
        VideoInfo videosInfo = baseMapper.getVideosInfo(videId);
//        从数据库查询
        if(videosInfo==null)throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
//        发布视频用户id|用户信息
        Long userId = videosInfo.getUserId();

        UserInfoVo userInfo = userServerImpl.getUserInfo(userId);
        videosInfo.setUserInfo(userInfo);

//        视频收藏|点赞|投币状态
        VideoStatus interactionStatus = getInteractionStatus(videId, loginIdAsLong);
        videosInfo.setVideoStatus(interactionStatus);
//        视频信息缓存
        if(redisUtils.tryLock( "video:info:lock:" + videId)){
            SystemConstants.CACHE_REBuILD_EXECUTOR.submit(()->{
             try {
                 updateVideoCache(videId,videosInfo);
             }catch (Exception e){
                 throw  new RuntimeException(e);
             }finally {
               redisUtils.unlock(videId);
             }
            });
        }

        return Result.success(videosInfo);
    }
//    更新缓存
    private void updateVideoCache(Long videId, VideoInfo videoInfo) {
        try {
            // 准备缓存数据
            VideoCacheDTO videoCacheDTO = new VideoCacheDTO();
            BeanUtils.copyProperties(videoInfo, videoCacheDTO);
            // 处理时间转换（存储为秒级时间戳）
            if (videoInfo.getPublishTime() != null) {
                videoCacheDTO.setPublishTime(videoInfo.getPublishTime().toEpochSecond(ZoneOffset.of("+8")));
            }

            // 转换为Map并缓存
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> redisMap = objectMapper.convertValue(videoCacheDTO, new TypeReference<>() {});

            // 缓存视频基础信息（设置合理的过期时间，如1小时）
            stringRedisTemplate.opsForHash().putAll(SystemConstants.REDIS_VIDEO_INFO+ videId, redisMap);
            stringRedisTemplate.expire(SystemConstants.REDIS_VIDEO_INFO + videId, 1, TimeUnit.HOURS);

            log.info("更新视频缓存成功，videoId={}", videoInfo);
        } catch (Exception e) {
            // 缓存更新失败，记录错误但不影响主业务
            log.error("更新视频缓存失败，videoId={}", videId, e);
        }
    }
//    获取缓存
    private VideoInfo getVideoCache(Long videId,Long LoUserId){
//        获取视频信息
        Map<Object, Object> videoInfoMap = stringRedisTemplate.opsForHash().entries(SystemConstants.REDIS_VIDEO_INFO+ videId);
        if (videoInfoMap.containsKey("publishTime")) {
            LocalDateTime dateTime = Instant.ofEpochSecond(Long.parseLong(videoInfoMap.get("publishTime").toString()) )
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            videoInfoMap.put("publishTime", dateTime);
        }
        VideoInfo videoInfo = BeanUtil.mapToBean(videoInfoMap, VideoInfo.class, false, new CopyOptions());
        if(!videoInfoMap.containsKey("userId"))return null;
//    视频状态获取
        String Login = String.valueOf(LoUserId);
        Boolean liked = stringRedisTemplate.opsForSet().isMember(SystemConstants.REDIS_VIDEO_LIKE + videId, Login);//用户视频点赞状态
        Double collected = stringRedisTemplate.opsForZSet().score(String.format(SystemConstants.REDIS_USER_COLLECT, Login),String.valueOf(videId));//用户收藏视频状态
        Double insert = stringRedisTemplate.opsForZSet().score(SystemConstants.REDIS_VIDEO_COIN + videId, Login);        //用户投币视频状态
        VideoStatus videoStatus = new VideoStatus();
        videoStatus.setLiked(BooleanUtil.isTrue(liked));
        videoStatus.setCollect(collected!=null);
        videoStatus.setInsert(insert!=null);
        videoInfo.setVideoStatus(videoStatus);


//        用户状态
        Long userId = videoInfo.getUserId();
        UserInfoVo userInfo = userServerImpl.getUserInfo(userId);
        videoInfo.setUserInfo(userInfo);
        return videoInfo;
    }

    @Override
    public VideoStatus getInteractionStatus(Long videoId, Long userId) {
        VideoStatus videoStatus = new VideoStatus();
        boolean collect = videoCollectMapper.exists(
                new QueryWrapper<VideoCollect>()
                        .eq("user_id", userId)
                        .eq("video_id", videoId)
                        .eq("status", 1)
        );
        boolean like = videoLikeMapper.exists(
                new QueryWrapper<VideoLike>()
                        .eq("user_id", userId)
                        .eq("video_id", videoId)
                        .eq("status", 1)
        );
        boolean coin = videoCoinMapper.exists(
                new QueryWrapper<VideoCoin>()
                        .eq("user_id", userId)
                        .eq("video_id", videoId)
        );
        videoStatus.setCollect(collect);
        videoStatus.setLiked(like);
        videoStatus.setInsert(coin);
        return videoStatus;
    }

    @Transactional
    @Override
    public Result giveLike(Long videId) {
//        获取登陆用户
        Long loginIdByToken = StpUtil.getLoginIdAsLong();
        String lockKey = "lock:collect:" + loginIdByToken + ":" + videId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(!lock.tryLock(1, 10, TimeUnit.SECONDS)){
                throw new Appexception(AppExceptionCodeMsg.OPERATION_TOO_FREQUENT);
            }
//        查询视频信息
            Videos videos = getById(videId);
            if(videos==null)throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);
            //缓存查询
            Boolean member = stringRedisTemplate.opsForSet().isMember(SystemConstants.REDIS_VIDEO_LIKE+videId, loginIdByToken.toString());
            VideoLike videoLike = new VideoLike();
            videoLike.setVideoId(videId);
            videoLike.setUserId(loginIdByToken);
            videoLike.setStatus((byte) 1);
//            未点赞
            if(BooleanUtil.isFalse(member)) {
//     数据库更新|视频信息缓存更新
                videoLikeMapper.insertOrUpdates(videoLike);
                updatelikeCache(videos,1);
                stringRedisTemplate.opsForSet().add(SystemConstants.REDIS_VIDEO_LIKE+videId,loginIdByToken.toString());
                return Result.success(null);
            }
//        已点赞
            videoLike.setStatus((byte) 0);
            updatelikeCache(videos,-1);
            videoLikeMapper.insertOrUpdates(videoLike);
            stringRedisTemplate.opsForSet().remove(SystemConstants.REDIS_VIDEO_LIKE+videId,loginIdByToken.toString());
            return Result.success(null);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new  RuntimeException(e);
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    private  void updatelikeCache(Videos videos,int number){
        //数据库视频点赞数量更新
        lambdaUpdate().eq(Videos::getVideoId, videos.getVideoId()).set(Videos::getLikeCount, videos.getLikeCount() + number).update();
//        缓存视频点赞数量更新
        stringRedisTemplate.opsForHash().increment(SystemConstants.REDIS_VIDEO_INFO+videos.getVideoId(),"likeCount",number);
//        缓存用户获赞数量更新
        stringRedisTemplate.opsForHash().increment(String.format(SystemConstants.REDIS_USER_INFO,videos.getUserId()),"likeCount",number);
    }

    @Transactional
    @Override
    public Result collcet(Long videId) {
//        登陆用户
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        String key = String.format(SystemConstants.REDIS_USER_COLLECT, loginIdAsLong);
        Double score = stringRedisTemplate.opsForZSet().score(key, String.valueOf(videId));
        VideoCollect videoCollect = new VideoCollect();
        videoCollect.setVideoId(videId);
        videoCollect.setUserId(loginIdAsLong);
        videoCollect.setStatus((byte) 1);

        String lockKey = "lock:collect:" + loginIdAsLong + ":" + videId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(!lock.tryLock(1, 10, TimeUnit.SECONDS)){
                throw new Appexception(AppExceptionCodeMsg.OPERATION_TOO_FREQUENT);
            }
            if(score==null){
                long newTime = LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant().getEpochSecond();
                videoCollectMapper.insertOrUpdates(videoCollect);
//            建立缓存
                stringRedisTemplate.opsForHash().increment(SystemConstants.REDIS_VIDEO_INFO+ videId,"collectCount",1);
                stringRedisTemplate.opsForZSet().add(key,String.valueOf(videId),newTime);
                return Result.success(null);
            }
//            移除缓存
            stringRedisTemplate.opsForZSet().remove(key,String.valueOf(videId));
            videoCollect.setStatus((byte) 0);
            stringRedisTemplate.opsForHash().increment(SystemConstants.REDIS_VIDEO_INFO+ videId,"collectCount",-1);
            videoCollectMapper.insertOrUpdates(videoCollect);
            return Result.success(null);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new  RuntimeException(e);
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public Result insertCoin(Long videoId, int coinCount) {
        // 1. 参数校验
        if (coinCount <= 0 || coinCount > 2) {
            throw new Appexception(AppExceptionCodeMsg.COIN_AMOUNT_INVALID);
        }

        // 2. 获取用户ID
        long userId = StpUtil.getLoginIdAsLong();
        String redisKey = SystemConstants.REDIS_VIDEO_COIN + videoId;
        String lockKey = "lock:coin:" + userId + ":" + videoId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                throw new Appexception(AppExceptionCodeMsg.OPERATION_TOO_FREQUENT);
            }
        // 3. 检查当前投币数
            Double currentScore = stringRedisTemplate.opsForZSet().score(redisKey, String.valueOf(userId));
            int currentCoins = currentScore != null ? currentScore.intValue() : 0;

            // 4. 校验总数
            if (currentCoins + coinCount > 2) {
                throw new Appexception(AppExceptionCodeMsg.COIN_AMOUNT_INVALID);
            }

            // 5. 数据库校验
            int dbCoins = videoCoinMapper.numberOfInvestmentsMade(videoId, userId);
            if (dbCoins + coinCount > 2) {
                throw new Appexception(AppExceptionCodeMsg.COIN_AMOUNT_INVALID);
            }

            // 6. 更新数据库
            VideoCoin videoCoin = new VideoCoin();
            videoCoin.setVideoId(videoId);
            videoCoin.setUserId(userId);
            videoCoin.setCoinCount((byte) coinCount);
            videoCoinMapper.insert(videoCoin);//更新数据库
            userServerImpl.getBaseMapper().updateCoinCount(videoId, coinCount);//更新视频作者硬币数量
            boolean is = userServerImpl.lambdaUpdate().eq(User::getId, userId)
                    .ge(User::getCoin, coinCount)
                    .setSql("coin = coin - " + coinCount)
                    .update();
            if(!is)throw new Appexception(AppExceptionCodeMsg.COIN_AMOUNT_INVALID);
            // 7. 更新Redis
            stringRedisTemplate.opsForZSet().incrementScore(redisKey, String.valueOf(userId), coinCount);
            stringRedisTemplate.opsForHash().increment(SystemConstants.REDIS_VIDEO_INFO+ videoId,"coinCount",coinCount);
            return Result.success(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new  RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 用户对当前视频可投币最大数量
     * @param videoId
     * @return
     */
    @Override
    public Result numberOfInvestmentsMade(Long videoId) {
//        登陆用户
        long userId = StpUtil.getLoginIdAsLong();
        Double score = stringRedisTemplate.opsForZSet().score(SystemConstants.REDIS_VIDEO_COIN + videoId, String.valueOf(userId));
//        获取登陆用户硬币数量
        User user = userServerImpl.getById(userId);
        VideoCoinVo videoCoinVo = new VideoCoinVo();
        videoCoinVo.setCoinCount(user.getCoin());

        if(score!=null){
//            缓存存在
            videoCoinVo.setMaxCoin(Math.max(0, 2 - score.intValue()));
            return  Result.success(videoCoinVo);
        }
//        缓存不存在
        int number = videoCoinMapper.numberOfInvestmentsMade(videoId, userId);
        videoCoinVo.setMaxCoin(Math.max(0, 2 - number));
        return Result.success(videoCoinVo);
    }

    @Override
    public Result comment(Long videoId,String content) {
        Long userId = StpUtil.getLoginIdAsLong(); //获取登陆用户

        boolean exists = lambdaQuery().eq(Videos::getVideoId, videoId).exists();
        if(!exists) throw new Appexception(AppExceptionCodeMsg.VIDEO_NOT_FOUND);

        User user = userServerImpl.getById(userId);
        Comment comment =new Comment();
        comment.setUserId(userId);
        comment.setVideoId(videoId);
        comment.setNickName(user.getNickName());
        comment.setContent(content);
        comment.setStatus((byte) 1);
        commentMapper.insert(comment);
//        返回评论信息
        CommentListVo commentList=new CommentListVo();
        BeanUtils.copyProperties(comment,commentList);
//        发评论用户信息
        CommentUserInfo commentUserInfo = copyCommentUserInfo(user);
        commentList.setUserInfo(commentUserInfo);
        return Result.success(commentList);
    }
    private CommentUserInfo copyCommentUserInfo(User user){
        CommentUserInfo commentUserInfo = new CommentUserInfo();
        commentUserInfo.setId(user.getId());
        commentUserInfo.setNickName(user.getNickName());
        commentUserInfo.setSignature(LocalDateTime.now());
        return commentUserInfo;
    }

    @Override
    public Result reply(ReplyParam replyParam) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        long commentId = Long.parseLong(replyParam.getCommentId());
        long replyUserId = Long.parseLong(replyParam.getReplyUserId());
        long pComment=Long.parseLong(replyParam.getPCommentId());
        log.info("{}",replyParam);
        // 一级评论回复
        boolean commentExists = Optional.ofNullable(commentMapper.selectById(commentId)).isPresent()
                || Optional.ofNullable(commentReplyMapper.selectById(commentId)).isPresent();

        if (!commentExists) {
            throw new Appexception(AppExceptionCodeMsg.COMMENT_NOT_FOUND);
        }

        User byId = userServerImpl.getById(replyUserId);
        User yId=userServerImpl.getById(loginIdAsLong);
//        被评论用户不存在
        if(byId==null)throw new Appexception(AppExceptionCodeMsg.COMMENT_USER_NOT_FOUND);

        CommentReply commentReply = new CommentReply();

//       回复用户
        commentReply.setUserId(loginIdAsLong);
//        回复用户昵称
        commentReply.setNickName(yId.getNickName());
//         被回复用户
        commentReply.setReplyUserId(replyUserId);
        // 被回复昵称
        commentReply.setReplyNickName(byId.getNickName());
//        被回复评论
        commentReply.setCommentId(commentId);
        commentReply.setPCommentId(pComment);
        commentReply.setReplyContent(replyParam.getContent());
        commentReply.setStatus((byte) 1);
        commentReplyMapper.insert(commentReply);
        LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getId, pComment)
                .setSql("reply_count = reply_count + 1");
            commentMapper.update(updateWrapper);
        return Result.success(null);
    }

    @Override
    public Result getComment(Long videoId, int pageSize,LocalDateTime lastTime) {
        List<CommentListVo> comments = commentMapper.getComment(videoId,lastTime,pageSize,2);
        QueryWrapper queryWrapper = new QueryWrapper<CommentListVo>()
                .eq("video_id", videoId);
//        总数据量
        Long total = commentMapper.selectCount(queryWrapper);
        return Result.success(        new PageResult<>(comments, total, pageSize));
    }

    @Override
    public Result getCommentReply(Long commentId, int pageSize, LocalDateTime lastTime) {
            List<Reply> replys=commentReplyMapper.getCommentReply(commentId,pageSize,lastTime);
            log.info("{}",replys);
            return Result.success(replys);
    }
}