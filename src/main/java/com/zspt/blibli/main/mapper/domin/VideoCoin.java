package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bli_video_coin")
public class VideoCoin {
    private Long id;

    private Long userId;

    private Long videoId;

    private byte coinCount;

    private LocalDateTime createTime;
}
