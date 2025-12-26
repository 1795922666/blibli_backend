package com.zspt.blibli.dynamic.controller.vo;

import com.zspt.blibli.common.vo.PageResult;
import lombok.Data;

import java.util.Date;

@Data
public class DynamicVo {


    private String description;

    private byte status; // 0 发动态 2 发视频

    private Long userId; //发布Id;

    private Date createAt; //发布时间

    private Long videoId; // 视频ID;


}
