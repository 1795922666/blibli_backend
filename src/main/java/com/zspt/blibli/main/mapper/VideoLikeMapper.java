package com.zspt.blibli.main.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zspt.blibli.main.mapper.domin.VideoLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {
    void insertOrUpdates(VideoLike videoLike);
}
