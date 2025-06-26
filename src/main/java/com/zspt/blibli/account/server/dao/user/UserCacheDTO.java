package com.zspt.blibli.account.server.dao.user;

import lombok.Data;

@Data
public class UserCacheDTO {
    private Long id;

    private String nickName;

    private String avatar;

    private String signature;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likeCount;
}
