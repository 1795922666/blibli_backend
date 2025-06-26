package com.zspt.blibli.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class   PageResult<T> {
   private T data;
   private long total;
   private  int pageSize;
}
