package com.zspt.blibli.account.controller.requestParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class UserParam {

    @Schema(name = "userName", description = "用户账号", example = "SUPER_ADMIN")
    private String userName;

    @Schema(name = "password", description = "密码", example = "SUPER_ADMIN")
    private String password;

}
