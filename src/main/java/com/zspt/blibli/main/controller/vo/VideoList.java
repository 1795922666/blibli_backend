package com.zspt.blibli.main.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class VideoList {
    private Long videoId;

    private  Long userId;

    private String title;

    private String coverUrl;

    private int categoryId;

    private String tags;

    private Long viewCount;

    private int danmakuCount;

    public LocalDateTime publishTime;

    private String nickName;

    private Long duration;

}
