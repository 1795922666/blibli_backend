package com.zspt.blibli.main.controller.requestParam;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class ReplyParam {
    private String pCommentId;

    private String commentId;

    private String replyUserId;

    private String content;
}
