package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.mapper.domin.UpFiles;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<UpFiles> {
}
