package com.zspt.blibli.main.controller.requestParam;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class ReplyParam {
    private String pCommentId;

    private String commentId;

    private String userId;

    private String nickName;

    private String replyUserId;

    private String replyNickName;

    private String content;
}
