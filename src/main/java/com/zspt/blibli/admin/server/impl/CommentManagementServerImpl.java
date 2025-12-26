package com.zspt.blibli.admin.server.impl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.admin.controller.requestParam.BatchCommentStatusParam;
import com.zspt.blibli.admin.controller.vo.Comment.CommentAuditVo;
import com.zspt.blibli.admin.server.CommentManagementServer;
import com.zspt.blibli.common.utils.CommentUtils;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.CommentMapper;
import com.zspt.blibli.main.mapper.domin.Comment;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Slf4j
public class CommentManagementServerImpl extends ServiceImpl<CommentMapper, Comment> implements CommentManagementServer {

    @Resource
    private CommentUtils commentUtils;

    @Override
    public Page<CommentAuditVo> getAuditCommentByPage(Integer pageNum, Long videoId) {
        Page<CommentAuditVo> page = new Page<>(pageNum == null ? 1 : pageNum,10);
        // 2. 调用Mapper，传入分页对象+可选videoId
        return  this.baseMapper.selectAuditCommentWithSpecifiedFormat(page, videoId);
    }

    /**
     * 批量审核通过（状态1）
     */
    @Transactional(rollbackFor = {Appexception.class, Exception.class})
    public Result batchPassComment(BatchCommentStatusParam param) {
       return commentUtils.batchUpdateCommentStatus(param, (byte) 1);
    }


    /**
     * 批量删除评论（状态2)
     */
    @Transactional(rollbackFor = {Appexception.class, Exception.class})
    public Result batchDeleteComment(BatchCommentStatusParam param) {
        return commentUtils.batchUpdateCommentStatus(param, (byte) 2);
    }




}
