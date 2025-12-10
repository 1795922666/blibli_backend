package com.zspt.blibli.admin.server.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.account.mapper.UserMapper;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.admin.controller.requestParam.AddUserParam;
import com.zspt.blibli.admin.controller.requestParam.UpdateUserParam;
import com.zspt.blibli.admin.controller.vo.UserDTO;
import com.zspt.blibli.admin.server.UserManagementServer;
import com.zspt.blibli.common.utils.SystemConstants;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserManagementServerImpl extends ServiceImpl<UserMapper, User> implements UserManagementServer {
    @Value("${file.default-avatar}")
    private String avatar;

    @Override
    public IPage<UserDTO> ActualizeUserInfo(Integer pageNum, Integer pageSize) {
        // 1. 构建分页对象：Page<实体类>(页码, 每页条数)
        Page<User> page = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询
        IPage<User> adminPage =this.getBaseMapper().selectPage(page, null);
        IPage<UserDTO> dtoPages   = adminPage.convert(admin->{
            UserDTO dtoPage=new UserDTO();
            BeanUtils.copyProperties(admin,dtoPage);
            return dtoPage;
        });
        return dtoPages ;
    }

    @Override
    public UserDTO ActualizeBIdUserInfo(Long id) {
        User byId = this.getById(id);
        UserDTO byDto = new UserDTO();
        BeanUtils.copyProperties(byId,byDto);

        return byDto;
    }

    @Override
    public Result ActualizeAddUser(AddUserParam user) {

//       用户名/手机号重复查询
       if(!ProvingUser(0L, user.getUserName(),user.getPhone()))throw  new Appexception(AppExceptionCodeMsg.USER_ALREADY_EXISTS);

//        密码加密
        PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);

//        头像地址为空设置默认头像地址
        user.setAvatar(StrUtil.isBlank(user.getAvatar()) ? avatar : user.getAvatar());
//        头像地址为空设置随机昵称
        user.setNickName(StrUtil.isBlank(user.getNickName())
                ? SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(6)
                : user.getNickName());

        User aUser=new User();
        BeanUtils.copyProperties(user,aUser);
        save(aUser);
        log.info("新增用户{}",aUser);
//        返回新增用户信息
        UserDTO reUserDto = new UserDTO();
        BeanUtils.copyProperties(user,reUserDto);

        return Result.success(reUserDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result ActualizeUpdateUser(UpdateUserParam user) {
//     1.  查询用户是否存在
        if(StrUtil.isBlank(user.getId()))throw  new Appexception(AppExceptionCodeMsg.NOT_NULL);
        long id =Long.parseLong(user.getId()) ;
        User byUser = getById(id);
        if(byUser==null)throw  new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
//      2.重复验证
        if(!ProvingUser(id,user.getUserName(),user.getPhone()))throw  new Appexception(AppExceptionCodeMsg.USER_ALREADY_EXISTS);
//密码加密
        if(StrUtil.isNotBlank(user.getPassword())){
            PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
            String encode = passwordEncoder.encode(user.getPassword());
            user.setPassword(encode);
        }

//        3.序列化
        CopyOptions copyOptions = CopyOptions.create()
                .setIgnoreNullValue(true)     // 忽略null值
                .setIgnoreError(true)         // 忽略错误
                .setFieldValueEditor((fieldName, fieldValue) -> {
                    if ("".equals(fieldValue)) {
                        return null;
                    }
                    return fieldValue;
                });
        BeanUtil.copyProperties(user, byUser, copyOptions);


//       4. 更新数据库
        updateById(byUser);
        log.info("修改用户{}",byUser);
        UserDTO reUserDto = new UserDTO();
        BeanUtils.copyProperties(byUser,reUserDto);
        return  Result.success("修改成功",reUserDto);
    }



    //    注销用户
    @Override
    public Result ActualizeDelUser(List<Long> ids) {
        // 1.双层非空 / 有效性校验
        if (CollectionUtil.isEmpty(ids)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }
        if (ids.contains(null)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        // 2.校验ID对应的用户是否存在
        long existCount = lambdaQuery().in(User::getId, ids).count();
        if (existCount != ids.size()) {
            log.warn("删除用户失败：部分ID不存在，请求ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }


        // 逻辑删除（推荐，更新status为禁用）
         boolean deleteSuccess = lambdaUpdate().in(User::getId, ids).set(User::getStatus, 2).update();

        if (!deleteSuccess) {
            log.error("删除用户失败：ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.USER_DELETE_FAILED);
        }

        log.info("删除用户成功：共删除{}个用户，ID={}", ids.size(), ids);
        return Result.success("删除成功，共删除" + ids.size() + "个用户");
    }

    @Override
    public Result AcualizeClosureUser(List<Long> ids) {
        // 1.双层非空 / 有效性校验
        if (CollectionUtil.isEmpty(ids)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }
        if (ids.contains(null)) {
            throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
        }

        // 2.校验ID对应的用户是否存在
        long existCount = lambdaQuery().in(User::getId, ids).count();
        if (existCount != ids.size()) {
            log.warn("封禁用户失败：部分ID不存在，请求ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }

        // 逻辑删除（推荐，更新status为禁用）
        boolean deleteSuccess = lambdaUpdate().in(User::getId, ids).set(User::getStatus, 0).update();

        if (!deleteSuccess) {
            log.error("封禁用户失败：ID={}", ids);
            throw new Appexception(AppExceptionCodeMsg.USER_DELETE_FAILED);
        }

        log.info("删除用户成功：共删除{}个用户，ID={}", ids.size(), ids);
        return Result.success("封禁成功，共封禁" + ids.size() + "个用户");
    }


//验证用户名/手机号是否重复
    public boolean ProvingUser(Long id,String userName,String phone ){
        if(id==0)return false;
        User one = lambdaQuery().eq(User::getUserName, userName).ne(User::getId, id).one();
        User two = lambdaQuery().eq(User::getPhone, phone).ne(User::getId, id).one();
        return one == null && two == null;
    }
}



