package com.zspt.blibli.account.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.zspt.blibli.account.controller.requestParam.UserParam;
import com.zspt.blibli.account.controller.vo.UserInfoVo;
import com.zspt.blibli.account.controller.vo.UserVo;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.account.server.impl.UserServerImpl;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name="ClientUM", description = "用户端用户管理")
public class UserController {
    @Resource
    private UserServerImpl userServer;

    @Operation( summary = "用户登陆")
    @PostMapping("/login")
    public Result login(@RequestBody UserParam user) {
        log.info(user.getUserName()+"用户登陆");
        return  userServer.login(user.getUserName(),user.getPassword());
    }

    @Operation(summary = "token登陆")
    @GetMapping("/token")
    public Result token() {
        String tokenValue = StpUtil.getTokenValue();
        return  userServer.getByToken(tokenValue);
    }

    @Operation(summary = "退出登陆")
    @GetMapping("/logout")
    public Result logout(){
        StpUtil.logout();
        return  Result.success(null);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<UserVo> getUserInfo(){
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        User user = userServer.getById(loginIdAsLong);
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);
        return Result.success(userVo);
    }

    @Operation(summary = "用户注册")
    @PutMapping("/register")
    public Result register(@RequestBody User user) {
      return   userServer.saveUser(user);
    }

    @Operation(summary = "修改用户密码")
    @PostMapping("/chagepassword")
    public Result chagePassword(@RequestParam("id") Long id, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword ) {
      return       userServer.chagePassword( id , oldPassword, newPassword);
    }

    @Operation(summary = "获取用户头像")
    @GetMapping("/getAvatara")
    public ResponseEntity getAvatara(@RequestParam String id) throws IOException {
        try {
            return   userServer.getAvatar(Long.parseLong(id));
        } catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.USER_AVATAR_NOT_FOUND);
        }
    }
    @Operation(summary = "根据用户id获取用户信息")
    @GetMapping("/{id}/userspace")
    public  Result getUserinfos(@PathVariable  String id){
            try {
                UserInfoVo userInfo = userServer.getUserInfo(Long.parseLong(id));
                return Result.success(userInfo);
            }catch (NumberFormatException e) {
                throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
            }
    }
    @Operation(summary = "用户关注")
    @PostMapping("/follow/{id}/{bol}")
    public Result follow(@PathVariable String id, @PathVariable boolean bol) {
        try {
            return  userServer.followeds(Long.valueOf(id) ,bol);
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }
    }
    @Operation(summary = "关注列表")
    @GetMapping("/{id}/followlist")
    public Result followList(@PathVariable String id){
        try {
            return userServer.followedList(Long.valueOf(id));
        }catch (NumberFormatException e) {
            throw new Appexception(AppExceptionCodeMsg.USER_NOT_FOUND);
        }
    }
}
