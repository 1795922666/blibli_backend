package com.zspt.blibli.common.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 单条曲线的系列数据封装类
 * 对应前端ECharts的series数组中的单个对象
 */
@Data
public class SeriesData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 曲线名称（用于前端图例展示）
     * 如："当日新增用户数"、"当日新增视频数"
     */
    private String name;

    /**
     * 曲线对应的纵坐标数值列表
     * 与xAxisData一一对应，格式：[0, 0, 120, 156, ...]
     * 使用Number类型兼容Integer、Long、Double等数值类型
     */
    private List<Number> data;

    /**
     * 图表类型（默认折线图）
     * 可选值："line"（折线图）、"bar"（柱状图）、"pie"（饼图）
     * 后端可指定，前端也可覆盖修改
     */
    private String type = "line";

    /**
     * 可选扩展：曲线颜色（16进制/RGB格式）
     * 如："#4895ef"（蓝色）、"rgb(249, 199, 79)"（黄色）
     */
    private String color;

    /**
     * 可选扩展：是否启用平滑曲线
     * true=平滑折线，false=直角折线，默认false
     */
    private Boolean smooth = false;
}