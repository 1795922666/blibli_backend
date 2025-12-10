package com.zspt.blibli.admin.controller.requestParam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description ="新增用户类")
public class AddUserParam {
    @Schema(description = "用户ID", example = "1", hidden = true)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "昵称", example = "鸡蛋糕", defaultValue = "用户_689754")
    private String nickName;

    @Schema(description = "头像地址", example = "default.jpg", defaultValue = "default.jpg")
    private String avatar;

    @Schema(description = "个性签名", example = "暂无签名")
    private String signature;


    @Schema(description = "硬币数", example = "0", defaultValue = "0")
    private Integer coin;

    @Schema(description = "手机号", defaultValue = "13800138000")
    private String phone;

    @NotBlank(message = "状态不能为空")
    @Schema(description = "状态：0封禁 1启用 2注销", defaultValue = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
