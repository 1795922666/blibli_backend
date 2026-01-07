package com.zspt.blibli.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuMessageDTO {
    private Long id;

    private String fromUserId;

    private String content;
}
