package com.zspt.blibli.main.controller.requestParam;
import com.zspt.blibli.main.dto.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageParam {
    private String type;

    private MessageDTO data;

    public Date createdAt;
}
