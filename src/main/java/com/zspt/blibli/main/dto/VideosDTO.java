package com.zspt.blibli.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideosDTO {
    private Long userId;

    private String title;

    private String description;

    private  int categoryID;

    private String coverUrl;

    private String tags;
}