package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("bli_dynamic_like")
public class DynamicLike {
   private long userId;

   private long dynamicId;

   private Date createTime;

   private int status; // 0 点赞 1 取消点赞
}
