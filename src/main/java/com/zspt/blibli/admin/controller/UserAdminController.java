package com.zspt.blibli.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zspt.blibli.admin.controller.requestParam.SystemUserParam;
import com.zspt.blibli.admin.mapper.domin.SysAmin;
import com.zspt.blibli.admin.server.UserAdminServer;
import com.zspt.blibli.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
@Tag(name="BackAdminController", description = "管理端用户管理")
public class UserAdminController {
        @Resource
        private UserAdminServer  userAdminServer;

        @Operation( summary = "管理员登陆")
        @PostMapping("/login")
        public Result login(@RequestBody SystemUserParam user) {
            log.info(user.getUsername()+"管理员登陆");
            return  userAdminServer.login(user.getUsername(),user.getPassword());
        }

        @Operation(summary = "管理员注册")
        @PutMapping("/register")
        public Result register(@RequestBody SysAmin user) {
            return   userAdminServer.saveAdmin(user);
        }

        @Operation(summary = "管理员token登陆")
        @GetMapping("/token")
        public Result token() {
            String tokenValue = StpUtil.getTokenValue();
            return  userAdminServer.getByToken(tokenValue);
        }


}
