package com.zspt.blibli.main.controller.vo.comment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentListVo {

    private Long id;

    private CommentUserInfo userInfo;

    private String content;

    private int likeCount;

    private boolean isLike;

    private List<Reply> reply=new ArrayList<>();

}
