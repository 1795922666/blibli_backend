package com.zspt.blibli.admin.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.admin.mapper.domin.SysAmin;
import com.zspt.blibli.common.vo.Result;

public interface UserAdminServer  extends IService<SysAmin>{

    Result login(String username, String password);

    Result saveAdmin(SysAmin user);

    Result getByToken( String tokenValue);



}
