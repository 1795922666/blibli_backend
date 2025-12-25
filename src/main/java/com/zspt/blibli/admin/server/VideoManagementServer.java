package com.zspt.blibli.admin.server;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.main.mapper.domin.Videos;

import java.util.List;

public interface VideoManagementServer extends IService<Videos> {

    public IPage<Videos> ActualizeVideoInfo(Integer pageNum, Integer pageSize);

    public Videos ActualizeBIdVideoInfo(long id);

    public Result  ActualizeUpVideo(List<Long> ids);

    public Result ActualizeDownVideo(List<Long> ids);

    public Result  ActualizeDelVideo(List<Long> ids);
}
