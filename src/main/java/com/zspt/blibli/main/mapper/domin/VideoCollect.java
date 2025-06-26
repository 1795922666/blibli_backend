package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bli_video_collect")
public class VideoCollect {
    private Long userId;

    private Long videoId;

    private byte status;//1有效 0取消收藏

    private LocalDateTime createTime;
}
