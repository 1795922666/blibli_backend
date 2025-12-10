package com.zspt.blibli.account.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    @Schema(name = "id" ,description = "用户id")
    private Long id;
    @Schema(name = "nickName" ,description = "用户昵称")
    private String nickName;
    @Schema(name = "avatar" ,description = "用户头像")
    private String avatar;
    

}
