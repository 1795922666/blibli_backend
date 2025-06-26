package com.zspt.blibli.main.controller.requestParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class FileParam {

    private String uid;

    private MultipartFile file;

    private String hash;

    private int index;

    private int total;
}
