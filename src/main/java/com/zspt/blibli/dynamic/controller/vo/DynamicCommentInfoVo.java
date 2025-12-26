package com.zspt.blibli.dynamic.controller.vo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Data;

@Data
public class DynamicCommentInfoVo {
    private Long id;
    private Long pcommentId;
    private Long userId;
    private String nickName;
    private Long dynamicId;
    private String content;
    private int likeCount;
    private int replyCount;
    private boolean userCount;
}
