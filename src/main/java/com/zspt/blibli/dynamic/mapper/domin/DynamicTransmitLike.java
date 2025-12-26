package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_dynamic_transmit_like")
public class DynamicTransmitLike {
    Long transmitId;
    Long userId;
    Date createTime;
    byte status;
}
