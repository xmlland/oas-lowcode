package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 反射工具
 */
public class ReflectUtils {
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * 通过字段名从对象或对象的父类中得到字段的值
     * @param object 对象实例
     * @param fieldName 字段名
     * @return 字段对应的值
     */
    public static Object getValue(Object object, String fieldName) throws Exception {
        if (object == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }

    /**
     * 通过字段名从对象或对象的父类中给某个字段赋值（调用字典的set方法）
     * @param object 对象实例
     * @param fieldName 字段名
     * @return 字段对应的值
     */
    public static Object setValue(Object object, String fieldName, Object value) throws Exception {
        if (object == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
                return object;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }

    public static Object setValue(Object object, String fieldName, String value) throws Exception {
        if (object == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
                return object;
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }


 /**
     * 通过字段名从对象或对象的父类中得到字段的值（调用字典的get方法）
     * @param object 对象实例
     * @param fieldName 字段名
     * @return 字段对应的值
  */
    public static Object getValueOfGet(Object object, String fieldName) {
        if (object == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getMethod = clazz.getMethod(getterName);
                return getMethod.invoke(object);
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * 通过字段名从对象或对象的父类中得到字段的值（调用字典的get方法，可以取出复杂的对象的值）
     * @param object 对象实例
     * @param fieldName 字段名
     * @return 字段对应的值
     */
    public static Object getValueOfGetIncludeObjectFeild(Object object, String fieldName) {

        if (object == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }

        if(HashMap.class.equals(object.getClass())){
            return ((Map<?, ?>)object).get(fieldName);
        }

        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                if (fieldName.contains(".")) {
                    // 如：operatorUser.name、operatorUser.org.name，递归调用
                    String[] splitFiledName = fieldName.split("\\.");
                    return getValueOfGetIncludeObjectFeild(
                            getValueOfGetIncludeObjectFeild(object, splitFiledName[0]),
                            splitFiledName[1]);
                }
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getMethod = clazz.getMethod(getterName);
                return getMethod.invoke(object);
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * map转实体类，key优先读取字段注解TableField
     */
    public static <T> LinkedHashMap<String, Object> toLinkedHashMapByTableField(T obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
            String fieldName = null;
            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof TableField) {
                    fieldName = ((TableField) annotation).value();
                    break;
                }
            }
            if (fieldName == null) {
                fieldName = field.getName();
            }
            result.put(fieldName, field.get(obj));
        }
        return result;
    }

    /**
     * 实体类转map，key优先读取字段注解TableField
     */
    public static <T> LinkedHashMap<String, Object> convertToMapByTableField(T entity) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
            String fieldName = field.getName();
            // if @TableField annotation is present, use it as the key instead of the field name
            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof TableField) {
                    fieldName = ((TableField) annotation).value();
                    break;
                }
            }
            try {
                Object value = field.get(entity);
                map.put(fieldName, value);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage());
            }
        }
        return map;
    }

    public static <T> T convertToEntityByTableField(LinkedHashMap<String, Object> map, Class<T> clazz) {
        try {
            T entity = clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // if @TableField annotation is present, use it as the key instead of the field name
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (annotation instanceof TableField) {
                        String annotationFieldName = ((TableField) annotation).value();
                        if (!annotationFieldName.isEmpty()) {
                            fieldName = annotationFieldName;
                        }
                        break;
                    }
                }

                if (map.containsKey(fieldName)) {
                    Object value = map.get(fieldName);

                    // convert the value to the field's type
                    Class<?> fieldType = field.getType();
                    if (value != null && !fieldType.isAssignableFrom(value.getClass())) {
                        if (fieldType == int.class || fieldType == Integer.class) {
                            value = Integer.parseInt(value.toString());
                        } else if (fieldType == long.class || fieldType == Long.class) {
                            value = Long.parseLong(value.toString());
                        } else if (fieldType == float.class || fieldType == Float.class) {
                            value = Float.parseFloat(value.toString());
                        } else if (fieldType == double.class || fieldType == Double.class) {
                            value = Double.parseDouble(value.toString());
                        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                            value = Boolean.parseBoolean(value.toString());
                        } else {
                            // handle other field types
                        }
                    }

                    field.set(entity, value);
                }
            }
            return entity;
        } catch (InstantiationException | IllegalAccessException e) {
            // handle exception
        }
        return null;
    }

    public static <T> JSONObject convertToJSONObjectByTableField(T entity) {
        JSONObject jsonObject = new JSONObject();
        Class<?> clazz = entity.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // if @TableField annotation is present, use it as the key instead of the field name
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (annotation instanceof TableField) {
                        String annotationFieldName = ((TableField) annotation).value();
                        if (!annotationFieldName.isEmpty()) {
                            fieldName = annotationFieldName;
                        }
                        break;
                    }
                }

                Object value = field.get(entity);
                jsonObject.put(fieldName, value);
            }
        } catch (IllegalAccessException e) {
            // handle exception
        }
        return jsonObject;
    }
}
