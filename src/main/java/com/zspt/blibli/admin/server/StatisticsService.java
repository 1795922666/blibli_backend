package com.zspt.blibli.admin.server;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zspt.blibli.admin.mapper.domin.NewAddChartData;
import com.zspt.blibli.common.vo.Result;

public interface StatisticsService  extends IService<NewAddChartData> {
    Result getNewAddTrend();
    Result getNewAddCount();
}
