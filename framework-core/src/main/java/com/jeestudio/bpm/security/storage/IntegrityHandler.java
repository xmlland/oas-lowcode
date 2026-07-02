package com.jeestudio.bpm.security.storage;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 完整性保护处理类（数据存储加密）
 */
public abstract class IntegrityHandler {

    /**
     * 对数据进行加密
     *
     * @param data    明文map
     * @param columns 需要加密的列
     * @return
     */
    public String mergeColumn(LinkedHashMap<String, Object> data, String... columns) {
        JSONObject jsonObject = new JSONObject();
        for (String column : columns) {
            Object value = data.get(column);
            if (value instanceof Zform) {
                value = ((Zform)value).getId();
            }
            if (value instanceof Map) {
                value = ((Map)value).get("id");
            }
            if (value != null) {
                jsonObject.put(column, value);
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 对数据进行加密
     *
     * @param data    明文map
     * @param columns 需要加密的列
     * @return
     */
    public String encrypt(LinkedHashMap<String, Object> data, String... columns) {

        return encrypt(mergeColumn(data, columns));
    }


    public abstract String encrypt(String data);

}
