package com.zspt.blibli.dynamic.controller.vo;

import lombok.Data;

@Data
public class DynamicCommentVo {

    private int pcommentId;
    private Long userId;
    private String nickName;
    private Long dynamicId;
    private String content;
}
