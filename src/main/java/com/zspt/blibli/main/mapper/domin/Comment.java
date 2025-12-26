package com.zspt.blibli.main.mapper.domin;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("bli_comment")
public class Comment {

    private Long id;

    private Long userId;

    private String nickName;

    private Long videoId;

    private String content;

    private int likeCount;

    private int replyCount;

    private byte status; //0待审核 1通过 2删除 3被举报

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
