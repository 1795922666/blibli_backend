package com.zspt.blibli.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileCreateDTO {

    private String fileHash;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private Long duration;

    private String format;

}
