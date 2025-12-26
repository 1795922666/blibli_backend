package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("bli_comment_reply")
public class CommentReply {
    @TableId
    private Long replyId;

    @TableField(value = "pComment_id")
    private  Long pCommentId;

    private Long commentId;

    private Long userId;

    private String nickName;

    private Long replyUserId;

    private String replyNickName;

    private String replyContent;

    private int likeCount;

    private byte status; //0待审核 1通过 2删除 3被举报

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
