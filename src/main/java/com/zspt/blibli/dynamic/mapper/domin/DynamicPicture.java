package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_dynamic_picture")
public class DynamicPicture {

    @TableId(type = IdType.ASSIGN_ID)
    private  Long pictureId;

    private Long dynamicId;

    private String pictureUrl;

    private Date createTime;
}
