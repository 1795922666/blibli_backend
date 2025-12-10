package com.zspt.blibli.admin.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;

    private String  userName;

    private String nickName;

    private String avatar;

    private String signature;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likeCount;

    private Integer coin;

    private String phone;
    //0封号 1启用 2注销
    private byte status;
}
