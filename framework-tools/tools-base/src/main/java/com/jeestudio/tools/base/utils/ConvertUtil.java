package com.jeestudio.tools.base.utils;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 类型转换工具
 */
public class ConvertUtil {
    public static String getString(Object object) {
        if (object != null) {
            return object.toString();
        } else {
            return "";
        }
    }

    public static Integer getInteger(Object object) {
        String value = getString(object);
        if (StrUtil.isNotEmpty(value)) {
            return Integer.parseInt(getString(object));
        } else {
            return null;
        }
    }

    public static Long getLong(Object object) {
        String value = getString(object);
        if (StrUtil.isNotEmpty(value)) {
            return Long.parseLong(getString(object));
        } else {
            return null;
        }
    }


    public static <K, V> Map<K, V> listToMap(List<V> list, Function<? super V, ? extends K> keyMapper) {
        Map<K, V> map = list.stream().collect(Collectors.toMap(keyMapper, a -> a, (k1, k2) -> k1));
        return map;
    }
}
