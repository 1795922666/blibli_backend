package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.controller.vo.comment.CommentListVo;
import com.zspt.blibli.main.mapper.domin.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
   List<CommentListVo> getComment(@Param("videoId") Long videoId, @Param("lastTime") LocalDateTime lastTime, @Param("pageSize") int pageSize,@Param("replyCount") int replyCount);
}
