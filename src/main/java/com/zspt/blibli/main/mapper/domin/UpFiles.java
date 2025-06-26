package com.zspt.blibli.main.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_videos_files")
public class UpFiles {

    private Long id;

    private Long videoId;

    private String fileHash;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private Long duration;

    private String format;

    private Date createdAt;
}
