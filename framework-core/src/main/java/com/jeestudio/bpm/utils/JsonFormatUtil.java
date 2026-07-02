package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.util.Map;

/**
 * @Description: JSON格式化工具
 */
public class JsonFormatUtil {



    public static String objectToStr(String value) {
        return objectToStr(value, new String[]{}, new String[]{});
    }

    public static String objectToStr(String value, String[] filedSort, String[] removeFiled) {
        if (StringUtil.isNotBlank(value)) {
            JSONObject jsonObject = JSONObject.parseObject(value);
            return toJSONStr(jsonObject, filedSort, removeFiled);
        }
        return value;
    }

    public static String arrayToStr(String value) {
        return arrayToStr(value, new String[]{}, new String[]{});
    }

    public static String arrayToStr(String value, String[] filedSort, String[] removeFiled) {
        if (StringUtil.isNotBlank(value)) {
            JSONArray jsonArray = JSONArray.parseArray(value);
            return toJSONStr(jsonArray, filedSort, removeFiled);
        }
        return value;
    }

    public static String toJSONStr(JSON value, String[] filedSort, String[] removeFiled) {
        JSON res = null;
        if (value instanceof JSONObject) {
            res = format((JSONObject) value, filedSort, removeFiled);
        }
        if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            jsonArray.forEach(item -> {
                if (item instanceof JSONObject) {
                    format((JSONObject) item, filedSort, removeFiled);
                }
            });
            res = jsonArray;
        }
        return toJSONStr(res);

    }
    public static String toJSONStr(JSON res) {
        String formatResult = com.alibaba.fastjson2.JSON.toJSONString(res,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.UseSingleQuotes,
                JSONWriter.Feature.UnquoteFieldName);
        formatResult = formatResult
                .replaceAll("\n\t", "")
                .replaceAll("\n", "")
                .replaceAll("\t", " ")
                .replaceAll("\\{ ", "\\{");
        return formatResult;
    }

    private static JSONObject format(JSONObject jsonObject, String[] filedSort, String[] removeFiled) {
        //删除指定的key
        for (String key : removeFiled) {
            jsonObject.remove(key);
        }
        //以指定的key顺序进行存储 放到新的jsonObject中 没有出现的放到后面 并且忽略空值及null
        JSONObject newJsonObject = new JSONObject(true);
        for (String key : filedSort) {
            if (jsonObject.containsKey(key) && StringUtil.isNotBlank(jsonObject.getString(key))) {
                newJsonObject.put(key, jsonObject.get(key));
            }
        }
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (!newJsonObject.containsKey(entry.getKey()) && StringUtil.isNotBlank(jsonObject.getString(entry.getKey()))) {
                newJsonObject.put(entry.getKey(), entry.getValue());
            }
        }
        return newJsonObject;
    }
}
