package com.zspt.blibli.main.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long toUserId;

    private String content;

}
