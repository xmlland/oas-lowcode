package com.jeestudio.bpm.common.entity.gen;

import lombok.Data;

import java.util.List;

/**
 * @Description: 代码生成列设置
 */
@Data
public class GenTableColumnSetting {

    /** 字段统计配置列表，用于列表页合计、分组计数等展示。 */
    List<StatisticsConfig> statisticsConfigList;


    /**
     * 统计配置
     */
    @Data
    public static class StatisticsConfig {

        /** 统计类型，例如合计或分组。 */
        private String type;
        /** 分组统计类型，例如计数。 */
        private String groupType;
        /** 数值统计精度。 */
        private Integer precision;
    }

    public enum StatisticsType {
        SUM,//合计
        GROUP//计数
    }

    public enum GroupType {
        COUNT,//计数
    }
}
