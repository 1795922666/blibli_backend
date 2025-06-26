package com.zspt.blibli.main.controller.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentUserInfo {
    private Long id;

    private String nickName;

    private LocalDateTime signature;
}
