package com.zspt.blibli.dynamic.controller.vo;

import com.zspt.blibli.dynamic.mapper.domin.DynamicPicture;
import lombok.Data;

import java.util.Date;

@Data
public class DynamicInfoVo {
    private String description;

    private byte status; // 0 发动态 2 发视频

    private Long userId; //发布Id;

    private Date createAt; //发布时间

    private Long videoId; // 视频ID;

    private DynamicPicture[] pictureArr; // 图片数据

    private int likeCount; // 点赞量;

    private int commentCount;

    private int transmitCount;

    private boolean userBoolean;

    private Long dynamicId;

}
