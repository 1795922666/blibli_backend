package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.controller.vo.comment.Reply;
import com.zspt.blibli.main.mapper.domin.CommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentReplyMapper extends BaseMapper<CommentReply> {
    List<Reply> getCommentReply(@Param("commentId") Long commentId, @Param("pageSize") int pageSize, @Param("lastTime") LocalDateTime lastTime);
}
