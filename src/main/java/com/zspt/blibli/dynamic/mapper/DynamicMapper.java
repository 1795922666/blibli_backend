package com.zspt.blibli.dynamic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.dynamic.mapper.domin.Dynamic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface DynamicMapper extends  BaseMapper<Dynamic> {

}
