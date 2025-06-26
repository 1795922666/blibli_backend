package com.zspt.blibli.account.controller.vo;

import lombok.Data;

@Data
public class UserInfoVo {
    private Long id;

    private String nickName;

    private String avatar;

    private String signature;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likeCount;
    /**
     * 查询用户和被查询用户关注状态
     */
    private boolean followed=false;
    /**
     * 查询用户是否位当前信息用户
     */
    private boolean isSelf=false;
}
