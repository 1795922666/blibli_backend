package com.zspt.blibli.main.controller.requestParam;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class VideosParam {
    private String uid;

    private String title;

    private String description;

    private  int categoryID;

    private MultipartFile cover;

    private String tags;
}
