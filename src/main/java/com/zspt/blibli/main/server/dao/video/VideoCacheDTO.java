package com.zspt.blibli.main.server.dao.video;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoCacheDTO {
    private Long userId;

    private Long videoId;

    private String title;

    private String description;

    private String fileUrl;

    private  int likeCount;

    private  int danmakuCount;

    private  int viewCount;

    private int coinCount;

    private int collectCount;

    private int commentCount;

    private Long publishTime;
}
