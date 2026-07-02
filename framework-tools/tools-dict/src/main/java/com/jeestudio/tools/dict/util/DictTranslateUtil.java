package com.jeestudio.tools.dict.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.base.annotation.Dict;
import com.jeestudio.tools.base.constant.CommonConstant;
import com.jeestudio.tools.base.utils.JSONHelper;
import com.jeestudio.tools.dict.DictHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @Description: 字典翻译工具
 */
public class DictTranslateUtil {

    /**
     * 翻译数据列表
     *
     * @param dictHandler
     * @param rows
     * @return
     */
    public static JSONArray translateDict(DictHandler dictHandler, List rows) {
        if (rows != null && rows.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            Class<?> aClass = rows.get(0).getClass();
            Field[] declaredFields = aClass.getDeclaredFields();
            Map<String, LinkedHashMap<Object, Object>> dataMap = new HashMap<>();
            Map<String, Dict> multipleMap = new HashMap<>();
            for (Field field : declaredFields) {
                Dict dict = field.getAnnotation(Dict.class);
                if (dict != null) {
                    LinkedHashMap<Object, Object> fieldDictionary = dictHandler.getDictionary(field);
                    dataMap.put(field.getName(), fieldDictionary);
                    if (dict.multiple()) {
                        multipleMap.put(field.getName(), dict);
                    }

                }
            }
            for (Object row : rows) {
                if (row == null) {
                    continue;
                }

                JSONObject newRow = new JSONObject();
                JSONObject json = JSONHelper.toJSONObject(row,true);
                newRow.putAll(json);
                for (Field field : declaredFields) {
                    String key = field.getName();
                    Object object = ReflectUtil.getFieldValue(row, field);
                    if (null != object) {
                        if (object instanceof List) {
                            JSONArray array = translateDict(dictHandler, (List) object);
                            newRow.put(key, array);
                        } else if (dataMap.containsKey(key)) {
                            newRow.put(key, object);
                            String value = Convert.toStr(object);
                            if (multipleMap.containsKey(key)) {
                                List<Object> res = new ArrayList<>();
                                String[] split = value.split(multipleMap.get(key).separator());
                                for (String s : split) {
                                    Object orDefault = dataMap.get(key).getOrDefault(s, null);
                                    if (orDefault != null) {
                                        res.add(orDefault);
                                    }
                                }
                                newRow.put(key + CommonConstant.dictSuffix, StringUtils.join(res, multipleMap.get(key).separator()));
                            } else {
                                Object o = dataMap.get(key).get(value);
                                newRow.put(key + CommonConstant.dictSuffix, o);
                            }
                        }
                    }
                }
                jsonArray.add(newRow);
            }
            return jsonArray;
        }
        return new JSONArray();
    }

    /**
     * 翻译数据对象 2022-07-29 16:54:49
     *
     * @param dictHandler
     * @param t
     * @return
     */
    public static <T> JSONObject translateDictObject(DictHandler dictHandler, T t) {
        if (t == null) {
            return null;
        }
        List<T> list = new ArrayList<>(1);
        list.add(t);
        return translateDict(dictHandler, list).getJSONObject(0);

    }
}
