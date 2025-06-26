package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
@Data
@TableName("bli_videos_categories")
public class Categories {
    private int categoryId;

    private String name;

    private int parentId;

    private int sort;

    private Date createdAt;

}
