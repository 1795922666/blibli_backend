package com.zspt.blibli.main.controller.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Reply {

    private Long replyId;

    private Long pCommentId;

    private Long commentId;

    private Long userId;

    private  String nickName;

    private String replyUserId;

    private String replyNickName;

    private String replyContent;

    private LocalDateTime replyUpdateTime;
    
}
