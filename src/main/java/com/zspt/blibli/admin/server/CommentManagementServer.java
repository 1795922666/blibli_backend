package com.zspt.blibli.admin.server;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.admin.controller.requestParam.BatchCommentStatusParam;
import com.zspt.blibli.admin.controller.vo.Comment.CommentAuditVo;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.mapper.domin.Comment;

public interface CommentManagementServer extends IService<Comment> {
    Page<CommentAuditVo> getAuditCommentByPage(Integer pageNum,Long videoId);

    Result batchPassComment(BatchCommentStatusParam param);

    Result batchDeleteComment(BatchCommentStatusParam param);
}
