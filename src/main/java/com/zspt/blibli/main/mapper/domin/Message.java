package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_chat_records")
public class Message {

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Date createdAt;

    private byte isRead;

}
