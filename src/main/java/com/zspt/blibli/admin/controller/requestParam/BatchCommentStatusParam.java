package com.zspt.blibli.admin.controller.requestParam;

import lombok.Data;

import java.util.List;
@Data
public class BatchCommentStatusParam {
    private List<SingleCommentStatusParam> commentList;
}
