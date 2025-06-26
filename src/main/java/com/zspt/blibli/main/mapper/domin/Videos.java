package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
@Data
@TableName("bli_videos")
public class Videos {
    @TableId
    private Long videoId;

    private  Long userId;

    private String title;

    private String description;

    private String coverUrl;

    private int categoryId;

    private String tags;

    private Long viewCount;

    private int danmakuCount;

    private int likeCount;

    private byte status; //0未审核 1已发布 2下架 3删除

    private Date publishTime;

    private Date createdAt;

    private Date updatedAt;

}
