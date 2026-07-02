package com.jeestudio.framework.wordexport.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Word导出请求参数
 */
@Data
public class RequestVo {
    Map<String, Object> configMap = new HashMap<>();
    Map<String, Object> dataMap = new HashMap<>();
}
