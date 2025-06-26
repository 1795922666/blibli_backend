package com.zspt.blibli.main.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.controller.vo.VideoInfo;
import com.zspt.blibli.main.controller.vo.VideoList;
import com.zspt.blibli.main.mapper.domin.Videos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideosMapper extends BaseMapper<Videos> {
     List<VideoList> recommend();

     VideoInfo getVideosInfo(@Param("videoId") Long videoId);
}
