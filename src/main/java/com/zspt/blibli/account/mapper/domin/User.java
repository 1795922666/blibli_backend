package com.zspt.blibli.account.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@TableName("bli_user")
public class User {
    @Schema(hidden = true)
    private Long id;

    @Schema(hidden = true)
    private String nickName;

    private String userName;

    private String password;

    private  String avatar;

    private String signature;

    private Integer followingCount;

    private Integer followerCount;

    private Integer likeCount;

    private Integer coin;

    private String phone;
}
