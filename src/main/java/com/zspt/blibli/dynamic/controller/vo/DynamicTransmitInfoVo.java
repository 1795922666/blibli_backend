package com.zspt.blibli.dynamic.controller.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DynamicTransmitInfoVo {

    private Long transmitId;
    private  Long userId;
    private Long dynamicId;
    private String description;
    private LocalDateTime createTime;

    private boolean userBoolean;
}
