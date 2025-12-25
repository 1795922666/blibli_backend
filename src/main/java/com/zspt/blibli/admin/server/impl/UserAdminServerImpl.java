package com.zspt.blibli.admin.server.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.account.server.impl.UserServerImpl;
import com.zspt.blibli.admin.mapper.UserAdminMapper;
import com.zspt.blibli.admin.mapper.domin.SysAmin;
import com.zspt.blibli.admin.server.UserAdminServer;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserAdminServerImpl extends ServiceImpl<UserAdminMapper, SysAmin> implements UserAdminServer {
    @Resource private UserServerImpl userServer;
   public Result login(String username, String password) {
       PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
//       验证用户是否存在
       SysAmin user = lambdaQuery().eq(SysAmin::getUsername, username).one();
       if(user == null)throw  new Appexception(AppExceptionCodeMsg.AUTH_FAILED);
//判断密码是否正确
       boolean matches = passwordEncoder.matches(password, user.getPassword());
       if(!matches)throw  new Appexception(AppExceptionCodeMsg.AUTH_FAILED);

       StpUtil.login(user.getId());

       return Result.success("登陆成功",null);
    }

    @Override
    public Result saveAdmin(SysAmin user) {
//       验证用户名重复
        SysAmin one = lambdaQuery().eq(SysAmin::getUsername, user.getUsername()).one();
        if(one!=null)throw  new Appexception(AppExceptionCodeMsg.USER_ALREADY_EXISTS);

//        密码加密
        PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        String encode = passwordEncoder.encode(user.getPassword());
        user.setPassword(encode);

        saveOrUpdate(user);
        log.info(user.getUsername()+"用户注册");
        return Result.success("注册成功",null);
    }

    public Result getByToken(String tokenValue) {
        Object loginIdByToken =  StpUtil.getLoginIdByToken(tokenValue);
        if(loginIdByToken==null)  throw new Appexception(AppExceptionCodeMsg.LOGIN_EXPIRED);
        return Result.success("登陆成功",null);
    }

}
