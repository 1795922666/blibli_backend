package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_dynamic")
public class Dynamic {

    private Long userId;
    @TableId(type = IdType.ASSIGN_ID)
    private Long dynamicId;

    private byte dynamicType;

    private String dynamicDescription;

    private Long dynamicVideoId;

    private Date createAt;

    private Date updateAt;

    private byte status;

    private int likeCount;

    private int commentCount;

    private int transmitCount;
}
