package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("bli_comment_reply")
public class CommentReply {
    private Long id;

    private  Long pCommentId;

    private Long commentId;

    private Long userId;

    private String nickName;

    private Long replyUserId;

    private String replyNickName;

    private String content;

    private int likeCount;

    private byte status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
