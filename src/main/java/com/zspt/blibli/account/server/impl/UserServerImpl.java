package com.zspt.blibli.account.server.impl;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zspt.blibli.account.controller.vo.UserInfoVo;
import com.zspt.blibli.account.mapper.UserMapper;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.account.server.UserServer;
import com.zspt.blibli.account.server.dao.user.UserCacheDTO;
import com.zspt.blibli.common.utils.FileUtils;
import com.zspt.blibli.common.utils.SystemConstants;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.config.FilePathConfig;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.domin.Follow;
import com.zspt.blibli.main.server.impl.FollowServerImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.AccessException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServerImpl extends ServiceImpl<UserMapper, User> implements UserServer {
    @Resource
    private FilePathConfig filePathConfig;

    @Resource
    private FollowServerImpl followServerImpl;
    @Resource
    private  StringRedisTemplate stringRedisTemplate;

    @Value("${file.default-avatar}")
    private String avatar;



    @Override
    public Result login(String username, String password) {
        PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        User user = lambdaQuery().eq(User::getUserName, username).one();
//        判断用户状态
        if(  user.getStatus() == 0)throw  new Appexception(AppExceptionCodeMsg.USER_STATUS_CLOSURE);
        if(  user.getStatus() == 2)throw  new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        if(user == null)throw  new Appexception(AppExceptionCodeMsg.AUTH_FAILED);
//        密码判断
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if(!matches)throw  new Appexception(AppExceptionCodeMsg.AUTH_FAILED);


        StpUtil.login(user.getId());
        return Result.success("登陆成功",null);
    }

    @Override
    public Result getByToken(String tokenValue) {
        Object loginIdByToken =  StpUtil.getLoginIdByToken(tokenValue);
       if(loginIdByToken==null)  throw new Appexception(AppExceptionCodeMsg.LOGIN_EXPIRED);
       return Result.success("登陆成功",null);
    }

    @Override
    public Result saveUser(User user) {
        User one = lambdaQuery().eq(User::getUserName, user.getUserName()).one();
        if(one!=null)throw  new Appexception(AppExceptionCodeMsg.USER_ALREADY_EXISTS);
        User one1 = lambdaQuery().eq(User::getPhone, user.getPhone()).one();
        if(one1!=null)throw  new Appexception(AppExceptionCodeMsg.USER_ALREADY_EXISTS);
        PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String encode = passwordEncoder.encode(user.getPassword());
//        随机昵称
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX+RandomUtil.randomString(6));
//        密码加密
        user.setPassword(encode);
//        添加默认头信息
        user.setAvatar(avatar);
        saveOrUpdate(user);
        log.info(user.getUserName()+"用户注册");
        return Result.success("注册成功",null);
    }

    @Override
    public Result chagePassword(Long id, String oldPassword, String newPassword) {
        // 1. 查询用户
        PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        User user = getById(id);
        if (user == null) {
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new Appexception(AppExceptionCodeMsg.DATA_CONFLICT); // 401
        }
        // 3. 更新密码（自动加密）
        lambdaUpdate()
                 .eq(User::getId, id)
                .set(User::getPassword, passwordEncoder.encode(newPassword))
                .update();

        return Result.success("密码修改成功");
    }

    /**
     * 获取用户头像
     * @param path
     * @return
     */
    @Override
    public ResponseEntity  getAvatar(String path) throws IOException {
        Path avatar = FileUtils.buildSafePath(filePathConfig.getAvatarPath(), path+".png");
        if(!Files.exists(avatar)) {
            avatar=FileUtils.buildSafePath( filePathConfig.getAvatarPath(), avatar.toString());
        }
        File file=avatar.toFile();
        FileSystemResource resource =  new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(avatar))
                .body(resource);
    }


//上传头像
    @Override
    public Result uploadAvatar(MultipartFile file) throws IOException {
        if (file.isEmpty())  throw  new Appexception(AppExceptionCodeMsg.FILE_EMPTY);

        // 2. 校验文件大小
        if (file.getSize() >  SystemConstants.MAX_AVATAR_SIZE) throw  new Appexception(AppExceptionCodeMsg.FILE_EMPTY);

        // 3. 校验文件格式（仅允许jpg/png/webp）
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀（如.jpg）
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!suffix.equalsIgnoreCase(".jpg") && !suffix.equalsIgnoreCase(".png") ) {
            throw  new Appexception(AppExceptionCodeMsg.FILE_EMPTY);
        }
//        生成唯一文件名
        String fileName = UUID.randomUUID() + suffix;
        File destFile = new File(filePathConfig.getAvatarPath()+ "/"+fileName);
        try {
            // 将上传文件写入目标路径
            file.transferTo(destFile);
            // 6. 返回头像访问URL
            return Result.success(fileName);
        } catch (Exception e) {
            throw new Appexception(AppExceptionCodeMsg.DATA_CONFLICT);
        }
    }

//    修改头像
    /**
     * 更新用户头像
     * @param id 用户ID（如 1001）
     * @param path 上传后的临时头像路径（如 /static/avatars/tmp_89757.png）
     * @return 统一返回结果
     */
    public String updateAvatar( String id, String path) {
        // ========== 1. 前置校验（空值/非法参数） ==========
        if (StrUtil.isBlank(id) || StrUtil.isBlank(path)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        Long userId = Long.valueOf(id);
        // ========== 2. 校验用户是否存在 ==========
        User user = getById(userId);
        if (user == null) {
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }

        if(path.equals(user.getAvatar()))return user.getAvatar();
        // ========== 3. 构建路径（新头像/旧头像） ==========
        // 3.1 提取新头像后缀（包含 .，如 .png）
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == path.length() - 1) {
            throw new Appexception(AppExceptionCodeMsg.FILE_TYPE_INVALID); // 无有效后缀
        }
        String suffix = path.substring(lastDotIndex); // 如 .png

        // 3.2 构建新头像最终路径（用户ID+后缀，如 /static/avatars/1001.png）
        String newAvatarFileName = userId + suffix;
        Path newAvatarPath = FileUtils.buildSafePath(filePathConfig.getAvatarPath(), newAvatarFileName);

        // 3.3 校验新头像路径是否已存在（避免覆盖）
        if (Files.exists(newAvatarPath)) {
            throw new Appexception(AppExceptionCodeMsg.FILE_EXISTS);
        }

        // 3.4 构建上传的临时头像路径（原始路径）
        Path tempAvatarPath = FileUtils.buildSafePath(filePathConfig.getAvatarPath(), path);
        if (!Files.exists(tempAvatarPath)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL); // 临时文件不存在
        }

        // ========== 4. 删除旧头像 ==========
        try {
            if (StrUtil.isNotBlank(user.getAvatar())) { // 旧头像路径非空才删除
                Path oldAvatarPath = FileUtils.buildSafePath(filePathConfig.getAvatarPath(), user.getAvatar());
                Files.deleteIfExists(oldAvatarPath); // 推荐用 deleteIfExists，无需捕获 NoSuchFileException
                log.info("删除用户旧头像成功，userId={}, oldPath={}", userId, oldAvatarPath);
            }
        } catch (Exception e) {
            // 非致命异常：记录日志，但不中断流程（避免因旧头像删除失败导致新头像更新失败）
            log.error("删除用户旧头像失败，userId={}", userId, e);
            // 可选：抛自定义异常，让前端提示（根据业务决定）
            // throw new AppException(AppExceptionCodeMsg.DELETE_OLD_AVATAR_FAILED);
        }

        // ========== 5. 重命名临时头像为最终路径（核心逻辑） ==========
        try {
            // 移动+重命名临时文件到最终路径（原子操作，支持覆盖，跨盘符兼容）
            Files.move(
                    tempAvatarPath,
                    newAvatarPath,
                    StandardCopyOption.ATOMIC_MOVE, // 原子操作，防文件损坏
                    StandardCopyOption.REPLACE_EXISTING // 兜底：即使偶发存在也覆盖（根据业务可选）
            );
            log.info("头像重命名成功，userId={}, tempPath={} → newPath={}", userId, tempAvatarPath, newAvatarPath);
        } catch (Exception e) {
            log.error("头像重命名失败，userId={}", userId, e);
            throw new Appexception(AppExceptionCodeMsg.FILE_UPLOAD_FAILED);
        }

        // ========== 6. 更新数据库 ==========
        user.setAvatar(newAvatarFileName);
        boolean updateSuccess = updateById(user);
        if (!updateSuccess) {
            // 数据库更新失败：回滚文件操作（删除刚重命名的新头像）
            try {
                Files.deleteIfExists(newAvatarPath);
            } catch (Exception ex) {
                log.error("回滚新头像文件失败，userId={}", userId, ex);
            }
            throw new Appexception(AppExceptionCodeMsg.FILE_UPLOAD_FAILED);
        }

        // ========== 7. 返回结果 ==========
        return newAvatarFileName;
    }
    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Override
    public UserInfoVo getUserInfo(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        String key = String.format(SystemConstants.REDIS_USER_INFO, id);
        UserInfoVo userInfo = new UserInfoVo();
        // 1. 尝试从缓存获取用户信息
        UserInfoVo userInfoVo = getUserFromCache(key);
        if (userInfoVo != null) {
            // 设置是否为当前用户和关注状态
            BeanUtils.copyProperties(userInfoVo, userInfo);
            processUserRelations(userInfo, currentUserId, id);
            log.info("用户信息缓存获取:{}",userInfo);
            return userInfo;
        }

        // 2. 缓存未命中，查询数据库
        User user = getById(id);
        if (user == null) {
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }

        // 3. 转换数据库对象到VO
        BeanUtils.copyProperties(user, userInfo);

        // 4. 设置是否为当前用户和关注状态
        processUserRelations(userInfo, currentUserId, id);

        // 5. 更新缓存
        int likeCount = getBaseMapper().getLikeCount(id);
        userInfo.setLikeCount(likeCount);
        updateUserCache(userInfo, key);

        return userInfo;
    }

    /**
     * 从缓存获取用户信息
     */
    private UserInfoVo getUserFromCache(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (CollectionUtils.isEmpty(entries)) {
            return null;
        }
        return BeanUtil.mapToBean(entries, UserInfoVo.class, false, new CopyOptions());
    }

    /**
     * 更新用户缓存
     */
    private void updateUserCache(UserInfoVo user, String key) {
        UserCacheDTO userCacheDTO = new UserCacheDTO();
        BeanUtils.copyProperties(user, userCacheDTO);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> redisMap = objectMapper.convertValue(userCacheDTO, new TypeReference<>() {});
        stringRedisTemplate.opsForHash().putAll(key, redisMap);
        stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    /**
     * 处理用户关系（是否为当前用户和关注状态）
     */
    private void processUserRelations(UserInfoVo userInfo, Long currentUserId, Long targetUserId) {
        // 判断是否为当前用户
        boolean isSelf = targetUserId.equals(currentUserId);
        userInfo.setSelf(isSelf);

        // 如果不是当前用户，查询关注状态
        if (!isSelf) {
            userInfo.setFollowed(followServerImpl.followed(currentUserId, targetUserId) != null);
        }
    }

    @Override
    @Transactional
    public Result followeds(Long id, boolean bol) {
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        Follow followed1 = followServerImpl.followed(loginIdAsLong, id);
//        bol为true时为关注为false时为取消关注
        if(bol&&followed1==null){
//            关注表添加数据
            Follow value=new Follow();
            value.setFollowingId(loginIdAsLong);
            value.setFollowerId(id);
            followServerImpl.save(value);

            lambdaUpdate().eq(User::getId, loginIdAsLong).setSql("follower_count = follower_count+1").update();
            lambdaUpdate().eq(User::getId, id).setSql("following_count = following_count+1").update();

            stringRedisTemplate.opsForZSet().add(String.format(SystemConstants.REDIS_USER_FOLLOW,loginIdAsLong),id.toString(), Instant.now().getEpochSecond());

        }else {
            followServerImpl.removeById(followed1.getId());
            lambdaUpdate().eq(User::getId, loginIdAsLong).gt(User::getFollowerCount,0).setSql("follower_count = follower_count-1").update();
            lambdaUpdate().eq(User::getId, id).gt(User::getFollowingCount,0).setSql("following_count = following_count-1").update();
            stringRedisTemplate.opsForZSet().remove(String.format(SystemConstants.REDIS_USER_FOLLOW,loginIdAsLong),id.toString());
        }
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result followedList(Long id) {
//        查看列表是否是登陆用户
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        if(loginIdAsLong!=id){
            return Result.success(null);
        }

        Set<String> followerIds = stringRedisTemplate.opsForZSet().reverseRange(
                String.format(SystemConstants.REDIS_USER_FOLLOW,id),
                0,
                -1
        );
        System.out.println(JSONUtil.toJsonStr(followerIds));
        // 直接转换，若集合为空则返回 null
        List<Long> userIds = Optional.ofNullable(followerIds)
                .filter(s -> !s.isEmpty())
                .map(s -> s.stream().map(Long::valueOf).collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        if(userIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<UserInfoVo> users = listByIds(userIds).stream()
                .map((e)->{
                    UserInfoVo userInfo = new UserInfoVo();
                    userInfo.setFollowed(true);
                    BeanUtils.copyProperties(e,userInfo);
                    return userInfo;
                }).toList();
        return Result.success(users);
    }

    @Override
    public long countNewUsersByTimeRange(Date startTime, Date endTime) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startTime, endTime)
                .eq("status", 1);
        return this.baseMapper.selectCount(queryWrapper);
    }


}