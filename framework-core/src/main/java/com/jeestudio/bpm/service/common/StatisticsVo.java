package com.jeestudio.bpm.service.common;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 统计视图对象
 */
@Data
public class StatisticsVo {

    Map<String, Object> sumMap;
    LinkedHashMap<String, LinkedHashMap<String, Object>> groupMap;
}
