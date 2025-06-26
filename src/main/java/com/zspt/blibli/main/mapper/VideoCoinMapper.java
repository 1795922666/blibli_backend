package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.mapper.domin.VideoCoin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoCoinMapper extends BaseMapper<VideoCoin> {
   int numberOfInvestmentsMade(@Param("videoId") Long videoId, @Param("userId") Long userId);
}
