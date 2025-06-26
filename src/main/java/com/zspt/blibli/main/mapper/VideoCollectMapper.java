package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.mapper.domin.VideoCollect;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoCollectMapper extends BaseMapper<VideoCollect> {
    void insertOrUpdates(VideoCollect videoCollect);
}
