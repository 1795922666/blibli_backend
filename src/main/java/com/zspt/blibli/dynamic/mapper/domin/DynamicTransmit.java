package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("bli_dynamic_transmit")
public class DynamicTransmit {
   private Long transmitId;
    private  Long userId;
    private Long dynamicId;
    private String description;
    private LocalDateTime createTime;
}
