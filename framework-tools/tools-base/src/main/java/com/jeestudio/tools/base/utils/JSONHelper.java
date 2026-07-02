package com.jeestudio.tools.base.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jeestudio.tools.base.exceptions.BusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: JSON工具
 */
public class JSONHelper {
    public static JSONObject toJSONObject(Object object) {
        return toJSONObject(object, true);
    }

    public static JSONObject toJSONObject(Object object, boolean writeMapNullValue) {
        try {
            String jsonStr;
            if (writeMapNullValue) {
                jsonStr = JSON.toJSONStringWithDateFormat(object, "yyyy-MM-dd HH:mm:ss",
                        SerializerFeature.WriteMapNullValue);
            } else {
                jsonStr = JSON.toJSONStringWithDateFormat(object, "yyyy-MM-dd HH:mm:ss");
            }
            return JSONObject.parseObject(jsonStr);
        } catch (Exception var4) {
            throw new BusinessException("数据处理异常" + ExceptionUtil.stacktraceToString(var4) + object);
        }
    }

    public static <T> JSONArray toJSONArray(List<T> list) {
        JSONArray result = new JSONArray();
        for (T t : list) {
            result.add(toJSONObject(t));
        }
        return result;
    }

    public static <T> List<T> toList(String text, Class<T> tClass) {
        JSONArray objects = JSONArray.parseArray(text);
        List<T> result = new ArrayList<>();
        for (Object object : objects) {
            result.add(JSONObject.parseObject(object.toString(), tClass));
        }
        return result;
    }
}
