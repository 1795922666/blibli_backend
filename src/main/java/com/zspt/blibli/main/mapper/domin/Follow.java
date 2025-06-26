package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bli_user_follow")
public class Follow {
    private Long id;

    private Long followingId;//关注者

    private Long followerId;//被关注者

    private Date createTime;

}
