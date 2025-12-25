package com.zspt.blibli.common.utils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zspt.blibli.account.mapper.UserMapper;
import com.zspt.blibli.account.mapper.domin.User;
import com.zspt.blibli.account.server.UserServer;
import com.zspt.blibli.admin.mapper.domin.NewAddChartData;
import com.zspt.blibli.admin.server.StatisticsService;
import com.zspt.blibli.main.mapper.VideosMapper;
import com.zspt.blibli.main.mapper.domin.Videos;
import com.zspt.blibli.main.server.VideosServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 视频/用户新增数量每日统计定时任务（单机版）
 */
@Slf4j
@Component
public class DailyNewAddStatisticsTask {

    @Resource
    private VideosServer  videosServer;
    @Resource
    private UserServer userServer;
    @Resource
    private StatisticsService statisticsService;

    /**
     * 每日凌晨1点执行（避开0点服务器负载高峰）
     * cron表达式：0 0 1 * * ? （秒 分 时 日 月 周 年，忽略年）
     * 测试时可改为：0 * * * * ? （每分钟执行，测试完成后改回正式表达式）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class) // 事务保障，避免部分数据写入失败
    public void statisticsDailyNewAdd() {
        log.info("开始执行每日视频/用户新增数量统计任务");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime yesterdayStart = LocalDateTime.of(yesterday, LocalTime.MIN); // 前一天00:00:00
            LocalDateTime yesterdayEnd = LocalDateTime.of(yesterday, LocalTime.MAX); // 前一天23:59:59.999999999

            // 转换为Date类型（适配MyBatis-Plus查询，若Mapper用LocalDateTime可直接使用）
            Date startDate = Date.from(yesterdayStart.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(yesterdayEnd.atZone(ZoneId.systemDefault()).toInstant());

            // 2. 统计前一天新增视频数量（按创建时间范围查询）
            long videoNewCount = videosServer.countNewVideosByTimeRange( startDate, endDate);
            log.info("前一天新增视频数量：{}", videoNewCount);

            // 3. 统计前一天新增用户数量
            long userNewCount = userServer.countNewUsersByTimeRange(startDate, endDate);
            log.info("前一天新增用户数量：{}", userNewCount);

            // 4. 查询统计表是否已有当天数据（按create_time范围匹配，确保唯一性）
            QueryWrapper<NewAddChartData> statQuery = new QueryWrapper<>();
            statQuery.between("create_time", startDate, endDate);
            NewAddChartData existStat = statisticsService.getOne(statQuery);

            // 5. 有则更新，无则插入
            NewAddChartData statistics = new NewAddChartData();
            statistics.setVideoCount((int) videoNewCount);
            statistics.setUserCount((int) userNewCount);
            statistics.setCreateTime(startDate); // 统计时间设为前一天00:00:00

            if (existStat != null) {
                // 更新已有数据
                UpdateWrapper<NewAddChartData> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", existStat.getId())
                        .set("video_count", videoNewCount)
                        .set("user_count", userNewCount);
                boolean updateSuccess = statisticsService.update(updateWrapper);
                if (updateSuccess) {
                    log.info("统计表已有当天数据，更新成功！");
                } else {
                    log.error("统计表数据更新失败！");
                }
            } else {
                // 插入新数据
                boolean saveSuccess = statisticsService.save(statistics);
                if (saveSuccess) {
                    log.info("统计表无当天数据，插入成功！");
                } else {
                    log.error("统计表数据插入失败！");
                }
            }

            log.info("每日视频/用户新增数量统计任务执行完成");
        } catch (Exception e) {
            log.error("每日视频/用户新增数量统计任务执行异常", e);
            throw e; // 抛出异常触发事务回滚
        }
    }
}