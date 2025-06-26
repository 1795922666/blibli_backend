package com.zspt.blibli.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {

    private String fileUrl;

    private String hash;

    private String total;

    private  String status;//0未合并1合并

    private String  expire_time;
}
