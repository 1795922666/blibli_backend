package com.zspt.blibli.common.utils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.zspt.blibli.admin.controller.requestParam.BatchCommentStatusParam;
import com.zspt.blibli.admin.controller.requestParam.SingleCommentStatusParam;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.enums.exceptionenu.AppExceptionCodeMsg;
import com.zspt.blibli.main.exception.Appexception;
import com.zspt.blibli.main.mapper.CommentMapper;
import com.zspt.blibli.main.mapper.CommentReplyMapper;
import com.zspt.blibli.main.mapper.domin.Comment;
import com.zspt.blibli.main.mapper.domin.CommentReply;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommentUtils {
    @Resource
    CommentReplyMapper commentReplyMapper;

    @Resource
    CommentMapper commentMapper;
    /**
     * 批量删除评论（状态3）
     */
    @Transactional(rollbackFor = {Appexception.class, Exception.class})
    public Result batchDeleteComment(BatchCommentStatusParam param) {
        return batchUpdateCommentStatus(param, (byte) 3);
    }

    private <T> void batchUpdateCommentStatus(
            BaseMapper<T> mapper,
            List<Long> ids,
            Byte targetStatus,
            SFunction<T, Long> idColumn,
            SFunction<T, Byte> statusColumn,
            String commentType
    ) {
        // 1. 空值校验
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        // 2. 执行批量更新
        LambdaUpdateWrapper<T> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(idColumn, ids)
                .set(statusColumn, targetStatus);
        int updateCount = mapper.update(null, updateWrapper);
        // 3. 校验更新行数：无效ID则抛自定义异常
        if (updateCount != ids.size()) {
            String errorMsg = String.format("%s存在无效ID：请求%d条", commentType, ids.size());
            log.error(errorMsg);
            throw new Appexception(AppExceptionCodeMsg.COMMENT_NOT_FOUND);
        }
    }


    public Result batchUpdateCommentStatus(BatchCommentStatusParam param, Byte targetStatus) {
        try {
            // 1. 基础参数校验
            if (CollectionUtils.isEmpty(param.getCommentList())) {
                throw new Appexception(AppExceptionCodeMsg.NOT_NULL);
            }
            // 2. 按level拆分数据
            Map<Integer, List<SingleCommentStatusParam>> levelGroup = param.getCommentList().stream()
                    .collect(Collectors.groupingBy(SingleCommentStatusParam::getLevel));

            // 3. 处理一级评论：复用通用方法
            if (levelGroup.containsKey(1)) {
                List<Long> firstCommentIds = levelGroup.get(1).stream()
                        .map(SingleCommentStatusParam::getCommentId)
                        .collect(Collectors.toList());
                batchUpdateCommentStatus(
                        commentMapper,          // 一级评论Mapper
                        firstCommentIds,        // ID列表
                        targetStatus,           // 目标状态（1/3/0）
                        Comment::getId,         // ID字段
                        Comment::getStatus,     // 状态字段
                        "一级评论"              // 类型描述
                );
            }

            // 4. 处理二级评论：复用通用方法
            if (levelGroup.containsKey(2)) {
                List<Long> secondCommentIds = levelGroup.get(2).stream()
                        .map(SingleCommentStatusParam::getCommentId)
                        .collect(Collectors.toList());
                batchUpdateCommentStatus(
                        commentReplyMapper,     // 二级评论Mapper
                        secondCommentIds,       // ID列表
                        targetStatus,           // 目标状态（1/3/0）
                        CommentReply::getReplyId,// ID字段
                        CommentReply::getStatus, // 状态字段
                        "二级评论"              // 类型描述
                );
            }
        } catch (Appexception e) {
            log.error("批量更新评论状态部分失败：{}", e.getMessage(), e);
            throw new Appexception(AppExceptionCodeMsg.COMMENT_NOT_FOUND);
        } catch (Exception e) {
            log.error("系统错误：{}", e.getMessage(), e);
            throw new Appexception(AppExceptionCodeMsg.PARAM_ERROR);
        }
        return  Result.success("更新成功");
    }
}
