package com.zspt.blibli.admin.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zspt.blibli.account.server.UserServer;
import com.zspt.blibli.admin.controller.vo.NewAddCountVo;
import com.zspt.blibli.admin.mapper.NewAddStatisticsMapper;
import com.zspt.blibli.admin.mapper.domin.NewAddChartData;
import com.zspt.blibli.admin.server.StatisticsService;
import com.zspt.blibli.common.vo.ChartData;
import com.zspt.blibli.common.vo.Result;
import com.zspt.blibli.common.vo.SeriesData;
import com.zspt.blibli.main.server.VideosServer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl extends ServiceImpl<NewAddStatisticsMapper, NewAddChartData> implements StatisticsService {

    @Resource
    private VideosServer videosServer;

    @Resource
    private UserServer userServer;

    private static final int DEFAULT_DAYS = 7;
    public Result getNewAddTrend() {
        // 1. 先查询数据库中的原始统计数据
        QueryWrapper<NewAddChartData> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        List<NewAddChartData> dbData = this.baseMapper.selectList(queryWrapper);

        // 2. 兜底逻辑：若查询无数据，自动生成默认数据并插入数据库
        if (CollectionUtils.isEmpty(dbData)) {
            System.out.println("统计表无数据，自动生成近" + DEFAULT_DAYS + "天默认数据...");
            // 构造默认数据列表
            List<NewAddChartData> defaultDataList = buildDefaultDataList();
            boolean saveSuccess = this.saveBatch(defaultDataList);
            if (saveSuccess) {
                System.out.println("默认数据插入成功，共" + defaultDataList.size() + "条");
                // 插入后重新查询，确保dbData有值
                dbData = this.baseMapper.selectList(queryWrapper);
            } else {
                System.err.println("默认数据插入失败，返回前端兜底空结构");
                // 插入失败时，直接返回内存中的默认数据（不依赖数据库）
                return Result.success(buildDefaultChartData(null));
            }
        }

        // 3. 有数据（原始数据/刚插入的默认数据），封装为曲线图格式返回
        ChartData chartData = buildDefaultChartData(dbData);
        return Result.success(chartData);
    }

    @Override
    public Result getNewAddCount() {
//        获取当前时间
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);
// 4. 转换为Date类型（适配MyBatis-Plus查询，若Mapper支持LocalDateTime可直接跳过转换）
        Date startDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());

        long uCount = userServer.countNewUsersByTimeRange(startDate, endDate);
        long vCount = videosServer.countNewVideosByTimeRange(startDate, endDate);

        NewAddCountVo countVo = new NewAddCountVo();

        countVo.setUserCount(uCount);
        countVo.setVideoCount(vCount);
        return Result.success(countVo);
    }


    private List<NewAddChartData> buildDefaultDataList() {
        List<NewAddChartData> defaultList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = DEFAULT_DAYS - 1; i >= 0; i--) {
            // 计算往前推i天的日期
            LocalDate targetLocalDate = currentDate.minusDays(i);
            Date targetDate = Date.from(targetLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            NewAddChartData statistics = new NewAddChartData();
            statistics.setVideoCount(0);
            statistics.setUserCount(0);
            statistics.setCreateTime(targetDate);
            defaultList.add(statistics);
        }
        return defaultList;
    }

    /**
     * 封装曲线图所需的ChartData格式（兼容有数据/无数据场景）
     */
    private ChartData buildDefaultChartData(List<NewAddChartData> dbData) {
        ChartData chartData = new ChartData();
        List<String> xAxisData = new ArrayList<>();
        List<Number> userData = new ArrayList<>();
        List<Number> videoData = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(dbData)) {
            // 有数据：从数据库数据中提取
            xAxisData = dbData.stream()
                    .map(item -> {
                        // 日期格式化：Date -> yyyy-MM-dd
                        LocalDate localDate = item.getCreateTime().toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        return localDate.toString();
                    })
                    .collect(Collectors.toList());

            userData = dbData.stream()
                    .map(NewAddChartData::getUserCount)
                    .collect(Collectors.toList());

            videoData = dbData.stream()
                    .map(NewAddChartData::getVideoCount)
                    .collect(Collectors.toList());
        } else {
            // 无数据（插入失败时）：构造内存兜底数据
            for (int i = DEFAULT_DAYS - 1; i >= 0; i--) {
                LocalDate targetDate = LocalDate.now().minusDays(i);
                xAxisData.add(targetDate.toString());
                userData.add(0);
                videoData.add(0);
            }
        }

        // 封装系列数据
        SeriesData userSeries = new SeriesData();
        userSeries.setName("当日新增用户数");
        userSeries.setData(userData);

        SeriesData videoSeries = new SeriesData();
        videoSeries.setName("当日新增视频数");
        videoSeries.setData(videoData);

        chartData.setXAxisData(xAxisData);
        chartData.setSeriesData(List.of(userSeries, videoSeries));
        return chartData;
    }
}
