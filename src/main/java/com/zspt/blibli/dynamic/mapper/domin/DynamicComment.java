package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bli_dynamic_comment")
public class DynamicComment {
    private Long id;

    private Long userId;

    private String nickName;

    private Long dynamicId;

    private int pcommentId;

    private String content;

    private int likeCount;

    private int replyCount;

    private byte status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
