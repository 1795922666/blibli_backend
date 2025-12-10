package com.zspt.blibli.admin.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.admin.controller.requestParam.AddUserParam;
import com.zspt.blibli.admin.controller.requestParam.UpdateUserParam;
import com.zspt.blibli.admin.controller.vo.UserDTO;
import com.zspt.blibli.common.vo.Result;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserManagementServer extends IService<User> {
    IPage<UserDTO> ActualizeUserInfo(Integer pageNum, Integer pageSiz);

    UserDTO ActualizeBIdUserInfo(Long id);

    Result ActualizeAddUser(AddUserParam user);

    Result ActualizeUpdateUser(UpdateUserParam user);

    Result ActualizeDelUser(List<Long> ids);

    Result AcualizeClosureUser(List<Long> ids);
}
