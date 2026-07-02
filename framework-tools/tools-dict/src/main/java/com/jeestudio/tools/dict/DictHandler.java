package com.jeestudio.tools.dict;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.base.annotation.Dict;
import com.jeestudio.tools.base.constant.CommonConstant;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.persist.PersistEnum;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * @Description: 字典处理器抽象类
 */
public abstract class DictHandler {

    public DictHandler() {

    }

    public LinkedHashMap<Object, Object> getDictionary(Field field) {
        Dict dict = field.getAnnotation(Dict.class);
        LinkedHashMap<Object, Object> dataMap = null;
        if (dict != null) {
            String dictCode = dict.dictCode();
            String dictTable = dict.dictTable();
            String dictValue = dict.dictValue();
            String dictText = dict.dictText();
            String orderCondition = dict.orderCondition();
            Class enumClass = dict.enumClass();
            String begin = dict.begin();
            String end = dict.end();
            String mapping = dict.mapping();
            Class dataDicClass = dict.dataDicClass();
            if (StrUtil.isNotEmpty(dictCode)) {
                return getDictionaryByDictCode(dictCode);
            } else if (StrUtil.isNotEmpty(dictTable) && StrUtil.isNotEmpty(dictValue) && StrUtil.isNotEmpty(dictText)) {
                JSONArray array = new JSONArray();
                if (StrUtil.isNotEmpty(orderCondition)) {
                    array = JSONArray.parseArray(orderCondition);
                }
                return getDictionaryByDictTable(dictTable, dictValue, dictText, array);
            } else if (StrUtil.isNotEmpty(begin) && StrUtil.isNotEmpty(end)) {
                if ("${nowYear}".equals(begin)) {
                    begin = String.valueOf(DateUtil.thisYear());
                }
                if ("${nowYear}".equals(end)) {
                    end = String.valueOf(DateUtil.thisYear());
                }
                return getDictionaryByRange(begin, end);
            } else if (StrUtil.isNotEmpty(mapping)) {
                return getDictionaryByMapping(JSONObject.parseObject(mapping));
            } else if (null != enumClass && String.class != enumClass) {
                return getDictionaryByEnumClass(enumClass);

            } else if (null != dataDicClass && String.class != dataDicClass) {
                return getDictionaryByDataDicClass(dataDicClass);

            }
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            dataMap = new LinkedHashMap<>();
            dataMap.put(CommonConstant.TRUE_STRING, CommonConstant.TRUE_TEXT);
            dataMap.put(CommonConstant.FALSE_STRING, CommonConstant.FALSE_TEXT);
        }
        return dataMap;
    }

    public abstract LinkedHashMap<Object, Object> getDictionaryByDictCode(String dictCode);

    public abstract LinkedHashMap<Object, Object> getDictionaryByDictTable(String dictTable, String dictValue, String dictText, JSONArray orderCondition);

    public LinkedHashMap<Object, Object> getDictionaryByRange(String begin, String end) {
        LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();

        int beginInt = Integer.parseInt(begin);
        int endInt = Integer.parseInt(end);
        for (int i = beginInt; i <= endInt; i++) {
            dataMap.put(String.valueOf(i), String.valueOf(i));
        }
        return dataMap;
    }

    public LinkedHashMap<Object, Object> getDictionaryByMapping(JSONObject mapping) {
        LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
        for (String key : mapping.keySet()) {
            dataMap.put(key, mapping.get(key));
        }
        return dataMap;
    }

    public LinkedHashMap<Object, Object> getDictionaryByEnumClass(Class enumClass) {
        LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
        Object[] enumConstants = enumClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            PersistEnum persistEnum = (PersistEnum) enumConstant;
            dataMap.put(Convert.toStr(persistEnum.getValue()), persistEnum.getText());
        }
        return dataMap;
    }

    public LinkedHashMap<Object, Object> getDictionaryByDataDicClass(Class dataDicClass) {
        LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
        try {
            Object instance = dataDicClass.newInstance();
            if (instance instanceof DataDic) {
                DataDic dataDic = (DataDic) instance;
                LinkedHashMap<Object, Object> dictionary = dataDic.getDictionary();
                return dictionary;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("getDictionaryByDataDicClass error");
        }
        return dataMap;
    }
}
