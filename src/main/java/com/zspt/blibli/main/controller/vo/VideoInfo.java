package com.zspt.blibli.main.controller.vo;

import com.zspt.blibli.account.controller.vo.UserInfoVo;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class VideoInfo {

    private UserInfoVo userInfo;

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

    private LocalDateTime publishTime;

    private VideoStatus videoStatus;

}
