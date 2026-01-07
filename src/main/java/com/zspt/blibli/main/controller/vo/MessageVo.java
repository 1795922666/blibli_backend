package com.zspt.blibli.main.controller.vo;

import com.zspt.blibli.main.dto.RuMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVo {
    private String type;

    RuMessageDTO data;

    private Date createdAt;
}
