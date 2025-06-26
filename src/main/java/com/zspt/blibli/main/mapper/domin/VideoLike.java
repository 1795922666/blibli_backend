package com.zspt.blibli.main.mapper.domin;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bli_video_like")
public class VideoLike {
    private Long videoId;

    private Long userId;

    private byte status;

    private LocalDateTime createTime;

}
