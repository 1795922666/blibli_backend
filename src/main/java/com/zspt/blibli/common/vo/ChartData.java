package com.zspt.blibli.common.vo;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ChartData implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号

    /**
     * 横坐标（x轴）数据列表
     * 格式：["2025-12-18", "2025-12-19", ...]，对应前端ECharts的xAxis.data
     * 存储日期/时间字符串，与后端统计时间粒度一致（按天/小时）
     */
    private List<String> xAxisData;

    /**
     * 纵坐标（y轴）多系列数据列表
     * 每个元素对应一条曲线（如“新增用户数”“新增视频数”），对应前端ECharts的series配置
     */
    private List<SeriesData> seriesData;
}