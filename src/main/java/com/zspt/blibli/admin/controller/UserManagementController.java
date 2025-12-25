package com.zspt.blibli.admin.controller;

import com.zspt.blibli.admin.controller.requestParam.AddUserParam;
import com.zspt.blibli.admin.controller.requestParam.UpdateUserParam;
import com.zspt.blibli.admin.server.impl.UserManagementServerImpl;
import com.zspt.blibli.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/userMan")
@Slf4j
@Tag(name="BackUserController", description = "用户管理")
public class UserManagementController {
    @Resource
    UserManagementServerImpl userManagementServer;
        @Operation(summary = "获取所有用户信息")
        @GetMapping("/allUserInfo")
        public Result AllUserInfo(Integer pageNum) {
            return Result.success(userManagementServer.ActualizeUserInfo(pageNum, 5));
        }

        @Operation(summary = "根据id获取用户信息")
        @GetMapping("/bIdUserInfo/{id}")
        public Result bIdUserInfo(@PathVariable  String id) {
            return Result.success(userManagementServer.ActualizeBIdUserInfo(Long.parseLong(id)));
        }

    @Operation(summary = "新增用户")
    @PutMapping("/addUser")
    public Result addUser(@RequestBody AddUserParam user) {
        return userManagementServer.ActualizeAddUser(user);
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/UpdateUser")
    public Result UpdateUser(@RequestBody UpdateUserParam user) {
        return userManagementServer.ActualizeUpdateUser(user);
    }

    @Operation(summary = "单个删除/批量删除用户")
    @DeleteMapping("/delUser")
    public Result delUser( @Parameter(description = "用户ID集合", required = true, example = "[1,2,3]")
                               @RequestBody List<Long> ids) {
           return userManagementServer.ActualizeDelUser(ids);
    }
    @Operation(summary = "单个封禁/批量封禁用户")
    @DeleteMapping("/cloUser")
    public Result cloUser( @Parameter(description = "用户ID集合", required = true, example = "[1,2,3]")
                           @RequestBody List<Long> ids) {
        return userManagementServer.AcualizeClosureUser(ids);
    }
}
