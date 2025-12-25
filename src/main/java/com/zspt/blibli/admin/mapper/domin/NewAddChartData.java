package com.zspt.blibli.admin.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_new_add_statistics")
public class NewAddChartData  {

    private String id;

    private Integer videoCount;

    private Integer userCount;

    private Date createTime;
}
