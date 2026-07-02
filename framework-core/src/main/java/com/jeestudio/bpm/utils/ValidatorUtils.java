package com.jeestudio.bpm.utils;

import cn.hutool.core.util.IdcardUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @Description: 正则校验工具
 */
public class ValidatorUtils {

    // 正则表达式常量
    private static final String REGEX_DIGITS = "^(-?\\d+)(\\.\\d+)?$";
    private static final String REGEX_NUMBER = "^(-?\\d+)(\\.\\d+)?$";
    private static final String REGEX_MOBILE = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
    private static final String REGEX_PHONE = "^(\\d{7,8}|(\\d{3,4}-)?\\d{7,8}(-\\d{1,4})?)$";
    private static final String REGEX_ZIPCODE = "^[0-9]\\d{5}(?!\\d)$";
    private static final String REGEX_EMAIL = "^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    private static final String REGEX_LONGITUDE = "^(0?\\d{1,2}(\\.\\d{1,8})?|1[0-7]?\\d{1}(\\.\\d{1,8})?|180(\\.0{1,8})?)$";
    private static final String REGEX_LATITUDE = "^([0-8]?\\d{1}(\\.\\d{1,8})?|90(\\.0{1,8})?)$";
    private static final String REGEX_PASSWORD = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&*]).{10,}$";
    private static final String REGEX_IDCARD = "^(\\d{15}|\\d{18}|\\d{17}[\\dxX])$";
    private static final String REGEX_USERNAME = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,18}$";

    // 字段名和验证方法的映射
    private static final Map<String, Function<String, Boolean>> VALIDATORS = new HashMap<>();
    // 字段名和中文错误提示的映射
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();

    static {
        // 初始化验证方法
        VALIDATORS.put("digits", value -> validateByRegex(value, REGEX_DIGITS));
        VALIDATORS.put("number", value -> validateByRegex(value, REGEX_NUMBER));
        VALIDATORS.put("mobile", value -> validateByRegex(value, REGEX_MOBILE));
        VALIDATORS.put("phone", value -> validateByRegex(value, REGEX_PHONE));
        VALIDATORS.put("telephone", value -> validateByRegex(value, REGEX_MOBILE) || validateByRegex(value, REGEX_PHONE));
        VALIDATORS.put("fax", value -> validateByRegex(value, REGEX_PHONE));
        VALIDATORS.put("zipCode", value -> validateByRegex(value, REGEX_ZIPCODE));
        VALIDATORS.put("email", value -> validateByRegex(value, REGEX_EMAIL));
        VALIDATORS.put("longitude", value -> validateByRegex(value, REGEX_LONGITUDE));
        VALIDATORS.put("latitude", value -> validateByRegex(value, REGEX_LATITUDE));
        VALIDATORS.put("password", value -> validateByRegex(value, REGEX_PASSWORD));
        VALIDATORS.put("idCard", ValidatorUtils::idCardValidate);
        VALIDATORS.put("username", value -> validateByRegex(value, REGEX_USERNAME));

        // 初始化中文错误提示信息
        ERROR_MESSAGES.put("digits", "请填写有效的数字");
        ERROR_MESSAGES.put("number", "请填写有效的数字格式");
        ERROR_MESSAGES.put("mobile", "请填写有效的手机号码");
        ERROR_MESSAGES.put("phone", "请填写有效的电话号码");
        ERROR_MESSAGES.put("telephone", "请填写有效的固定电话或手机号码");
        ERROR_MESSAGES.put("fax", "请填写有效的传真号码");
        ERROR_MESSAGES.put("zipCode", "请填写有效的邮政编码");
        ERROR_MESSAGES.put("email", "请填写有效的邮箱地址");
        ERROR_MESSAGES.put("longitude", "请填写有效的经度");
        ERROR_MESSAGES.put("latitude", "请填写有效的纬度");
        ERROR_MESSAGES.put("password", "密码至少包含10个字符，且包括字母、数字和特殊字符");
        ERROR_MESSAGES.put("idCard", "请填写有效的身份证号");
        ERROR_MESSAGES.put("username", "用户名必须是6到18位的字母和数字组合");
    }

    /**
     * 验证指定字段值是否符合规则
     *
     * @param fieldName 字段名
     * @param value     待验证的值
     * @return 是否符合规则
     */
    public static boolean validateField(String fieldName, String value) {
//        fieldName = StringEscapeUtils.unescapeJava(fieldName);
        Function<String, Boolean> validator = VALIDATORS.get(fieldName);
        if (validator != null) {
            return validator.apply(value);
        } else {
            // 校验fieldName是否是正则格式字符串
            if (isValidRegex(fieldName)) {
                // 如果fieldName是正则表达式，则使用正则验证
                return validateByRegex(value, fieldName);
            }
        }
        return true;
    }

    // 判断一个字符串是否符合正则规则
    public static boolean isValidRegex(String str) {
        try {
            // 尝试编译正则表达式
            Pattern.compile(str);
            return true; // 编译成功，说明是一个合法的正则表达式
        } catch (Exception e) {
            // 如果正则表达式编译失败，说明不是一个合法的正则表达式
            return false;
        }
    }

    public static boolean idCardValidate(String value) {
        return IdcardUtil.isValidCard(value);
    }

    /**
     * 获取字段对应的错误信息
     *
     * @param fieldName 字段名
     * @return 中文错误提示信息
     */
    public static String getErrorMessage(String fieldName) {
        return ERROR_MESSAGES.getOrDefault(fieldName, "无效的值。");
    }

    /**
     * 通用的正则验证方法
     *
     * @param value 待验证的值
     * @param regex 正则表达式
     * @return 是否符合正则
     */
    private static boolean validateByRegex(String value, String regex) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.matches(regex);
    }

    public static void main(String[] args) {
        // 测试验证
        String fieldName = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$";
        String value = "91110106678232729Y";

        if (!validateField(fieldName, value)) {
            System.out.println(getErrorMessage(fieldName)); // 输出中文错误信息
        } else {
            System.out.println("验证通过！");
        }

        // 测试错误信息查询
        System.out.println(getErrorMessage("email")); // 请填写有效的邮箱地址。
        System.out.println(getErrorMessage("unknownField")); // 无效的值。
    }
}
