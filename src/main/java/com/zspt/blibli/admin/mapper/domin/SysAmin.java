package com.zspt.blibli.admin.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("bli_sys_admin")
public class SysAmin {
    @Schema(hidden = true)
    private String id;

    private String username;

    private String password;
}
