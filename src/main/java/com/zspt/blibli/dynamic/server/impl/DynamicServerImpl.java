package com.zspt.blibli.dynamic.server.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zspt.blibli.account.server.impl.UserServerImpl;
import com.zspt.blibli.common.utils.FileUtils;
import com.zspt.blibli.common.utils.SystemConstants;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.dynamic.controller.vo.*;
import com.zspt.blibli.dynamic.mapper.*;
import com.zspt.blibli.dynamic.mapper.domin.*;
import com.zspt.blibli.dynamic.server.DynamicPictureServer;
import com.zspt.blibli.dynamic.server.DynamicServer;
import com.zspt.blibli.main.config.FilePathConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.dynamic.DynamicType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.zspt.blibli.account.controller.vo.UserInfoVo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DynamicServerImpl extends ServiceImpl<DynamicMapper,Dynamic> implements DynamicServer{


    // 注入JSON解析工具
    private final ObjectMapper objectMapper;
    @Autowired
    private DynamicMapper dynamicMapper;

    public DynamicServerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Resource
    private UserServerImpl userServer;


    @Resource
    private FilePathConfig filePathConfig;

    @Resource
    private DynamicPictureServerImpl dynamicPictureServer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DynamicLikeMapper dynamicLikeMapper;

    @Resource
    private DynamicCommentMapper dynamicCommentMapper;

    @Resource
    private DynamicCommentLikeMapper dynamicCommentLikeMapper;

    @Resource
    private DynamicTransmitMapper dynamicTransmitMapper;

    @Resource
    private DynamicTransmitLikeMapper dynamicTransmitLikeMapper;


    @Override
    public Result addDynamic(String  dynamicVo, List<MultipartFile> files) {

        //        查看列表是否是登陆用户
//        long loginIdAslong = StpUtil.getLoginIdAsLong();
//        if(loginIdAslong != id) {
//            return Result.success(null);
//        }



        DynamicVo dynamic;

        try {
            dynamic =  objectMapper.readValue(dynamicVo,DynamicVo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(String.valueOf(dynamic));

        Dynamic info = new Dynamic();
        if(dynamic.getStatus() == 0) {
            info.setDynamicType((byte) 0);
            info.setDynamicDescription(dynamic.getDescription());


        }else if(dynamic.getStatus() == 1) {
            info.setDynamicType((byte) 1);
            info.setDynamicVideoId(dynamic.getVideoId());
        }
//        BeanUtils.copyProperties(info,dynamic);
        info.setStatus((byte) 1);
        info.setCreateAt(dynamic.getCreateAt());
        info.setUserId(dynamic.getUserId());
        info.setUpdateAt(dynamic.getCreateAt());
        log.info(String.valueOf(info));
        boolean success = this.saveOrUpdate(info);
        stringRedisTemplate.opsForZSet().add(String.format(SystemConstants.REDIS_DYNAMIC_RECOMMEND),info.getDynamicId().toString(),System.currentTimeMillis() * 1.0);
        log.info("创建动态成功");

        if(success) {
            log.info(filePathConfig.getPicturePath());
            // 文件
            for (MultipartFile file : files) {
                try {
                    File filed = new File(filePathConfig.getPicturePath()  + "/" + UUID.randomUUID() + FileUtils.getFileExtension(file.getOriginalFilename()));
                    file.transferTo(filed);
                    DynamicPicture dynamicPicture = new DynamicPicture();
                    dynamicPicture.setDynamicId(info.getDynamicId());
                    dynamicPicture.setPictureUrl(filed.getName());
                    dynamicPicture.setCreateTime(new Date());

                    // sql存储
                    dynamicPictureServer.addPicture(dynamicPicture);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        return Result.success("动态发布成功",null);
    }



    @Override
    public Result deleteDynamic(Long id) {
        return null;
    }

    @Override
    public Result dynamicAllInfo(Integer num) {
        // 获取数据

        return Result.success(getDynamicByPage(num)) ;
    }

    @Override
    public Result dynamicSingleInfo(Long userId,Integer num) {
        return null;
    }


    /**
     *  动态点赞
     * */
    @Override
    public Result dynamicLike(Long id,byte status) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        if(status == 1) {
            DynamicLike dynamicLike1 = dynamicLikeMapper.selectOne(new QueryWrapper<DynamicLike>().eq("user_id", loginIdAsLong).eq("dynamic_id", id));

            if(dynamicLike1 != null) {
                dynamicLikeMapper.update(null,new UpdateWrapper<DynamicLike>().eq("user_id",loginIdAsLong).eq("dynamic_id",id).set("status",1));


            } else {
                DynamicLike dynamicLike = new DynamicLike();
                dynamicLike.setDynamicId(id);
                dynamicLike.setStatus(status);
                dynamicLike.setUserId(loginIdAsLong);
                dynamicLikeMapper.insert(dynamicLike);

            }
            dynamicMapper.update(null,new LambdaUpdateWrapper<Dynamic>().eq(Dynamic::getDynamicId,loginIdAsLong).setSql("like_count = like_count + 1"));
            return Result.success("点赞成功");
        } else {
            dynamicLikeMapper.update(null,new UpdateWrapper<DynamicLike>().eq("user_id",loginIdAsLong).eq("dynamic_id",id).set("status",0));

            dynamicMapper.update(null,new LambdaUpdateWrapper<Dynamic>().eq(Dynamic::getDynamicId,loginIdAsLong).setSql("like_count = like_count - 1"));

        }
        return Result.error(500,"服务器报错",null);
    }


    /**
     * 动态评价
     * */
    @Override
    public Result dynamicComment(DynamicCommentVo dynamicCommentVo) {

            DynamicComment dynamicComment1 = new DynamicComment();
            BeanUtils.copyProperties(dynamicCommentVo,dynamicComment1);
            log.info("dynamicCOmment = {}",dynamicComment1);
            LocalDateTime now = LocalDateTime.now();
            dynamicComment1.setCreateTime(now);
            dynamicComment1.setUpdateTime(now);
            dynamicCommentMapper.insert(dynamicComment1);

        dynamicMapper.update(null,new UpdateWrapper<Dynamic>().eq("dynamic_id",dynamicCommentVo.getDynamicId()).setSql("comment_count = comment_count + 1"));



        return Result.success("评价成功");
    }

    @Override
    public Result dynamicCommentLike(Long userid, Long commentId,byte status) {

        if(status == 1) {
            DynamicCommentLike dynamicCommentLike1 = dynamicCommentLikeMapper.selectOne(new QueryWrapper<DynamicCommentLike>().eq("user_id", userid).eq("comment_id", commentId));

            if(dynamicCommentLike1 == null) {
                DynamicCommentLike dynamicCommentLike = new DynamicCommentLike();
                dynamicCommentLike.setCommentId(commentId);
                dynamicCommentLike.setUserId(userid);
                dynamicCommentLikeMapper.insert(dynamicCommentLike);
            } else {
                dynamicCommentLikeMapper.update(null,new UpdateWrapper<DynamicCommentLike>().eq("user_id", userid).eq("comment_id",commentId).set("status",1));
            }
            dynamicCommentMapper.update(null,new UpdateWrapper<DynamicComment>().eq("comment_id",commentId).setSql("like_count = like_count + 1"));

            return Result.success("评论点赞成功");

        }else if(status == 0) {
            dynamicCommentLikeMapper.update(null,new UpdateWrapper<DynamicCommentLike>().eq("user_id",userid).eq("comment_id",commentId).set("status",0));


            dynamicCommentMapper.update(null,new UpdateWrapper<DynamicComment>().eq("comment_id",commentId).setSql("like_count = like_count - 1"));
            return Result.success("取消点赞");
        }

        return Result.error(500,"服务器报错",null);
    }

    /**
     * 动态转发
     * @param userId
     * @param dynamicId
     * @param description
     * @return
     */
    @Override
    public Result dynamicTransmit(Long userId, Long dynamicId, String description) {
        DynamicTransmit dynamicTransmit = new DynamicTransmit();
        dynamicTransmit.setDynamicId(dynamicId);
        dynamicTransmit.setUserId(userId);
        dynamicTransmit.setDescription(description);
        dynamicTransmit.setCreateTime(LocalDateTime.now());

        dynamicTransmitMapper.insert(dynamicTransmit);

        dynamicMapper.update(null,new UpdateWrapper<Dynamic>().eq("dynamic_id",dynamicId).setSql("transmit_count = transmit_count + 1"));

        return Result.success("转发成功");
    }

    @Override
    public Result dynamicTransmitLike(Long transmitId, byte status) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        if(status == 1) {
            DynamicTransmitLike dynamicTransmitLike = dynamicTransmitLikeMapper.selectOne(new QueryWrapper<DynamicTransmitLike>().eq("user_id", loginIdAsLong).eq("transmit_id", transmitId));

            if(dynamicTransmitLike == null) {
                dynamicTransmitLikeMapper.update(null,new UpdateWrapper<DynamicTransmitLike>().eq("user_id",loginIdAsLong).eq("transmit_id",transmitId).set("status",1));
            } else {
                DynamicTransmitLike dynamicTransmitLike1 = new DynamicTransmitLike();
                dynamicTransmitLike1.setTransmitId(transmitId);
                dynamicTransmitLike1.setStatus(status);
                dynamicTransmitLike1.setUserId(loginIdAsLong);
                dynamicTransmitLikeMapper.insert(dynamicTransmitLike1);
            }
            return Result.success("点赞成功");
        } else {
            dynamicTransmitLikeMapper.update(null,new UpdateWrapper<DynamicTransmitLike>().eq("user_id",loginIdAsLong).eq("transmit_id",transmitId).set("status",0));
        }

        return Result.error(500,"服务器报错",null);
    }

    @Override
    public Result dynamicCommentList(Long id) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        // 获取评论列表
        List<DynamicComment> dynamicId = dynamicCommentMapper.selectList(new QueryWrapper<DynamicComment>().eq("status",0).eq("dynamic_id", id));
        List<Long> list = dynamicId.stream().map(DynamicComment::getId).toList();

        LambdaQueryWrapper<DynamicCommentLike> queryWrapper = Wrappers.lambdaQuery(DynamicCommentLike.class)
                .eq(DynamicCommentLike::getUserId,loginIdAsLong)
                .in(DynamicCommentLike::getCommentId, list);


        List<Long> list1 = dynamicCommentLikeMapper.selectList(queryWrapper).stream().map(DynamicCommentLike::getCommentId).toList();


        List<DynamicCommentInfoVo> list2 = dynamicId.stream().map(e -> {
            boolean contains = list1.contains(e.getId());
            DynamicCommentInfoVo dynamicCommentInfoVo = new DynamicCommentInfoVo();
            BeanUtils.copyProperties(e, dynamicCommentInfoVo);
            dynamicCommentInfoVo.setUserCount(contains);
            return dynamicCommentInfoVo;
        }).toList();

//        DynamicCommentInfoVo dynamicCommentInfoVo = new DynamicCommentInfoVo()


        return Result.success(list2);
    }

    @Override
    public Result dynamicTransmitList(Long id) {

        long loginIdAsLong = StpUtil.getLoginIdAsLong();

        List<DynamicTransmit> dynamicTransmits = dynamicTransmitMapper.selectList(new LambdaQueryWrapper<DynamicTransmit>().eq(DynamicTransmit::getDynamicId, id));

        List<Long> list = dynamicTransmits.stream().map(DynamicTransmit::getDynamicId).toList();

        LambdaQueryWrapper<DynamicTransmitLike> in = Wrappers.lambdaQuery(DynamicTransmitLike.class)
                .eq(DynamicTransmitLike::getUserId, loginIdAsLong)
                .in(DynamicTransmitLike::getTransmitId, list);

        List<Long> list1 = dynamicTransmitLikeMapper.selectList(in).stream().map(DynamicTransmitLike::getTransmitId).toList();

        List<DynamicTransmitInfoVo> list2 = dynamicTransmits.stream().map(e -> {
            boolean contains = list1.contains(e.getTransmitId());
            DynamicTransmitInfoVo dynamicTransmitInfoVo = new DynamicTransmitInfoVo();
            BeanUtils.copyProperties(e, dynamicTransmitInfoVo);
            dynamicTransmitInfoVo.setUserBoolean(contains);
            return dynamicTransmitInfoVo;
        }).toList();

        return Result.success(list2);
    }


    /**
     * 查询距离现在最近的动态
     * @return 最新的20条动态列表
     */
    public List<DynamicInfoVo> getDynamicByPage(Integer pageNum) {
        //        查看列表是否是登陆用户
//        long loginIdAsLong = StpUtil.getLoginIdAsLong();
//        if(loginIdAsLong!=id){
//            return null;
//        }
        Object o = redisTemplate.opsForValue().get(SystemConstants.REDIS_DYNAMIC_CACHE_KEY);
        List<DynamicInfoVo> filteredList = null;
        if(o instanceof List) {
            filteredList =(List<DynamicInfoVo>) o;
        }


        if(filteredList != null && !filteredList.isEmpty()) {
            int num = pageNum - 1;
            List<DynamicInfoVo> dynamics = filteredList.subList(0, 20);

            redisTemplate.opsForValue().set(SystemConstants.REDIS_DYNAMIC_CACHE_KEY, filteredList.subList(20,filteredList.size()),6, TimeUnit.HOURS);

            return dynamics;
        }


        //全部动态
        Set<String> userId = stringRedisTemplate.opsForZSet().reverseRange(SystemConstants.REDIS_DYNAMIC_RECOMMEND, 0, -1);

        //直接转换
        List<Long> userIds = Optional.ofNullable(userId).filter((e) -> !e.isEmpty()).map(a -> a.stream().map(Long::valueOf).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        log.info("userIds - 1 {}",userIds);

        // 获取动态
        List<Dynamic> dynamics = listByIds(userIds).stream()
                .map(e -> {
                    log.info("user 0{}", e);
                    Dynamic dynamic = new Dynamic();
                    BeanUtils.copyProperties(e, dynamic);
                    return dynamic;
                }).toList();
        log.info("dynamics1 {}" ,dynamics);



        // 获取关注用户
        List<Long> Idlist = getUserIdArr();
        log.info("dynamics2 {}" ,Idlist);

        // 最终动态
        List<Dynamic> list = dynamics.stream().filter(e -> {
            boolean contains = Idlist.contains(e.getUserId());
            log.info("con3 {} {}", contains,e.getUserId());
            return contains;
        }).toList();

        log.info("最终 dynamic4 {}", list);
        ArrayList<Dynamic> dynamics1 = new ArrayList<>(list);

        List<DynamicInfoVo> dynamicInfoVos2 = AllDispose(dynamics1);
        //video/cover

        if(dynamicInfoVos2.size() > 20) {
            redisTemplate.opsForValue().set(SystemConstants.REDIS_DYNAMIC_CACHE_KEY,dynamics1.subList(20,dynamicInfoVos2.size()));
            return dynamicInfoVos2.subList(0,20);
        }else {
            return dynamicInfoVos2;
        }
    }


    /**
     * 查询用户距离现在最近的动态
     * @return 最新的20条动态列表
     */
    public List<DynamicInfoVo> getDynamicById(Long userId,Integer pageNum) {

        List<Dynamic> list = list(new LambdaQueryWrapper<Dynamic>().eq(Dynamic::getUserId, userId));
        List<DynamicInfoVo> dynamicInfoVos = AllDispose(new ArrayList<>(list));
        if(dynamicInfoVos.size() > 20) {
            redisTemplate.opsForValue().set(SystemConstants.REDIS_DYNAMIC_CACHE_KEY,dynamicInfoVos.subList(20,dynamicInfoVos.size()));
            return dynamicInfoVos.subList(0,20);
        }else {
            return dynamicInfoVos;
        }
    }

    // 获取关注用户
    public List<Long> getUserIdArr() {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        Object result = userServer.followedList(loginIdAsLong).getData();
        List<UserInfoVo> sourceList = List.of();
        if(result instanceof List) {
            List<UserInfoVo> list = new ArrayList<>((List<UserInfoVo>) result) ;
            // 自己的动态
            UserInfoVo userInfoVo = new UserInfoVo();
            userInfoVo.setId(loginIdAsLong);
            list.add(userInfoVo);
            sourceList = list;
        }

        return sourceList.stream().map(UserInfoVo::getId).toList();
    }


    // 处理图片
    public List<DynamicInfoVo>  PictureDispose(ArrayList<Dynamic> dynamics1,ArrayList<Long> longs) {
        List<DynamicPicture> dynamicPictures = dynamicPictureServer.queryByNonPrimaryIds(longs);
        log.info("dynamicPictures",dynamicPictures.toString());
        //
        return dynamics1.stream().map(e -> {
            //
            List<DynamicPicture> list2 = dynamicPictures.stream().filter(s -> Objects.equals(s.getDynamicId(), e.getDynamicId())).toList();
            DynamicPicture[] array = list2.toArray(new DynamicPicture[0]);
            // 转化对象
            DynamicInfoVo dynamicInfoVo = new DynamicInfoVo();
            dynamicInfoVo.setStatus(e.getStatus());
            dynamicInfoVo.setCreateAt(e.getCreateAt());
            dynamicInfoVo.setUserId(e.getUserId());
            dynamicInfoVo.setLikeCount(e.getLikeCount());
            dynamicInfoVo.setVideoId(e.getDynamicVideoId());
            dynamicInfoVo.setDescription(e.getDynamicDescription());
            dynamicInfoVo.setDynamicId(e.getDynamicId());
            dynamicInfoVo.setPictureArr(array);
            return dynamicInfoVo;

        }).toList();
    }

    // 处理点赞
    public List<DynamicInfoVo>  LikeDispose(ArrayList<DynamicInfoVo> dynamics1) {
        if(dynamics1.isEmpty()) {
            return new ArrayList<DynamicInfoVo>();
        }
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        List<Long> list = dynamics1.stream().map(DynamicInfoVo::getDynamicId).toList();
        LambdaQueryWrapper<DynamicLike> dynamicLikeLambdaQueryWrapper = Wrappers.lambdaQuery(DynamicLike.class)
                .in(DynamicLike::getDynamicId,list);

        List<DynamicLike> dynamicLikes = dynamicLikeMapper.selectList(dynamicLikeLambdaQueryWrapper);
        log.info("点赞量 {}",dynamicLikes);
       return   dynamics1.stream().peek(item -> {

            long count = dynamicLikes.stream().filter(e -> item.getDynamicId() == e.getDynamicId()).count();
            item.setLikeCount((int) count);
           long count1 = dynamicLikes.stream().filter(e -> e.getDynamicId() == item.getDynamicId()).filter(e -> e.getUserId() == loginIdAsLong).count();
           if(count1 != 0)
               item.setUserBoolean(true);
       }).toList();
    }

    // 处理转发
    public List<DynamicInfoVo>  TransmitDispose(ArrayList<DynamicInfoVo> dynamics1,ArrayList<Long> longs) {
        if(dynamics1.isEmpty()) {
            return new ArrayList<DynamicInfoVo>();
        }
        List<Long> list = dynamics1.stream().map(DynamicInfoVo::getDynamicId).toList();

        LambdaQueryWrapper<DynamicTransmit> in = Wrappers.lambdaQuery(DynamicTransmit.class)
                .in(DynamicTransmit::getDynamicId,list);

        List<DynamicTransmit> dynamicTransmits = dynamicTransmitMapper.selectList(in);
        log.info("转发量 {}",dynamicTransmits);

        return   dynamics1.stream().peek(item -> {
            long count = dynamicTransmits.stream().filter(e -> Objects.equals(item.getDynamicId(), e.getDynamicId())).count();
            item.setTransmitCount((int) count);
        }).toList();
    }

    // 处理评论
    public List<DynamicInfoVo>  CommentDispose(ArrayList<DynamicInfoVo> dynamics1,ArrayList<Long> longs) {
        if(dynamics1.isEmpty()) {
            return new ArrayList<DynamicInfoVo>();
        }
        List<Long> list = dynamics1.stream().map(DynamicInfoVo::getDynamicId).toList();

        LambdaQueryWrapper<DynamicComment> in = Wrappers.lambdaQuery(DynamicComment.class)
                .in(DynamicComment::getDynamicId,list);

        List<DynamicComment> dynamicComments = dynamicCommentMapper.selectList(in);
        log.info("评论量 {}",dynamicComments);

        return   dynamics1.stream().peek(item -> {
            long count = dynamicComments.stream().filter(e -> Objects.equals(item.getDynamicId(), e.getDynamicId())).count();
            item.setCommentCount((int) count);
        }).toList();
    }


    // 总处理
    public List<DynamicInfoVo>  AllDispose(ArrayList<Dynamic> dynamics1) {
        if(dynamics1.isEmpty()) {
            return new ArrayList<DynamicInfoVo>();
        }
        // 获取图片
        ArrayList<Long> longs = new ArrayList<>(dynamics1.stream().map(Dynamic::getDynamicId).toList());
        // 图片
        List<DynamicInfoVo> list1 = PictureDispose(dynamics1, longs);

        // 点赞
        List<DynamicInfoVo> dynamicInfoVos = LikeDispose(new ArrayList<>(list1));
        // 转发
        List<DynamicInfoVo> dynamicInfoVos1 = TransmitDispose(new ArrayList<>(dynamicInfoVos), longs);


        // 评价
        List<DynamicInfoVo> dynamicInfoVos2 = CommentDispose(new ArrayList<>(dynamicInfoVos1), longs);

        return dynamicInfoVos2;

    }


    // 动态审核
    public Result dynamiCaudit(Long dynamic, byte status){
        Dynamic byId = getById(dynamic);
        if(byId == null) {
            return Result.error(3,"传入无效ID",null);
        }
        byId.setStatus(status);
        boolean b = updateById(byId);

        return Result.success("审核成功");
    }

    // 动态审核列表
    public Result dynamicCauditList() {
        Set<String> userId = stringRedisTemplate.opsForZSet().reverseRange(SystemConstants.REDIS_DYNAMIC_RECOMMEND, 0, -1);
        List<Long> userIds = Optional.ofNullable(userId).filter((e) -> !e.isEmpty()).map(a -> a.stream().map(Long::valueOf).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        log.info("userIds - 1 {}",userIds);
        List<Dynamic> dynamics = listByIds(userIds).stream()
                .map(e -> {
                    log.info("user 0{}", e);
                    Dynamic dynamic = new Dynamic();
                    BeanUtils.copyProperties(e, dynamic);
                    return dynamic;
                }).toList();
        return Result.success(dynamics);
    }

}






