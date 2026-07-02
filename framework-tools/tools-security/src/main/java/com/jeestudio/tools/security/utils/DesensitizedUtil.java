package com.jeestudio.tools.security.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.jeestudio.tools.base.annotation.Security;
import com.jeestudio.tools.base.enums.DesensitiseTypeEnum;
import com.jeestudio.tools.security.config.DesensitizedConfig;
import com.jeestudio.tools.security.pojo.DesensitizedResult;
import com.jeestudio.tools.security.pojo.SM4EncryptResult;
import com.jeestudio.tools.security.utils.security.SM4Util;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 脱敏工具
 */
public class DesensitizedUtil {

    /**
     * 姓名脱敏
     *
     * @param personName 姓名
     * @return
     */
    public static String personName(String personName) {
        return StrUtil.isBlank(personName) ? StrUtil.EMPTY : StrUtil.hide(personName, 0, 1);
    }

    /**
     * 姓名脱敏并加密
     *
     * @param personName 姓名
     * @return
     */
    public static DesensitizedResult personNameEncrypt(String personName) {
        SM4EncryptResult encrypt = SM4Util.encrypt(personName);
        String result = personName(personName);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 姓名脱敏并加密
     *
     * @param encryptKey 密钥
     * @param personName 姓名
     * @return
     */
    public static DesensitizedResult personNameEncrypt(String encryptKey, String personName) {
        String encrypt = SM4Util.encrypt(encryptKey, personName);
        String result = personName(personName);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 身份证号 普通隐藏脱敏
     *
     * @param idCard 身份证号
     * @return
     */
    public static String idCard(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return StrUtil.EMPTY;
        }
        if (idCard.length() == 15) {
            return StrUtil.hide(StrUtil.hide(idCard, 1, 4), 10, 14);
        }
        if (idCard.length() == 18) {
            return StrUtil.hide(StrUtil.hide(idCard, 1, 4), 12, 17);

        }
        return StrUtil.EMPTY;
    }

    /**
     * 身份证号 普通隐藏脱敏并加密
     *
     * @param idCard 身份证号
     * @return
     */
    public static DesensitizedResult idCardEncrypt(String idCard) {
        SM4EncryptResult encrypt = SM4Util.encrypt(idCard);
        String result = idCard(idCard);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 身份证号 普通隐藏脱敏并加密
     *
     * @param idCard 身份证号
     * @return
     */
    public static DesensitizedResult idCardEncrypt(String encryptKey, String idCard) {
        String encrypt = SM4Util.encrypt(encryptKey, idCard);
        String result = idCard(idCard);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 身份证号 强隐藏脱敏
     *
     * @param idCard 身份证号
     * @return
     */
    public static String idCardStrong(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return StrUtil.EMPTY;
        }
        if (idCard.length() == 15) {
            return StrUtil.hide(idCard, 1, 14);
        }
        if (idCard.length() == 18) {
            return StrUtil.hide(idCard, 1, 17);

        }
        return StrUtil.EMPTY;
    }

    /**
     * 身份证号 强隐藏脱敏并加密
     *
     * @param idCard 身份证号
     * @return
     */
    public static DesensitizedResult idCardStrongEncrypt(String idCard) {
        SM4EncryptResult encrypt = SM4Util.encrypt(idCard);
        String result = idCardStrong(idCard);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 身份证号 强隐藏脱敏并加密
     *
     * @param idCard 身份证号
     * @return
     */
    public static DesensitizedResult idCardStrongEncrypt(String encryptKey, String idCard) {
        String encrypt = SM4Util.encrypt(encryptKey, idCard);
        String result = idCardStrong(idCard);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 军官证号、护照号等其他身份证件脱敏
     *
     * @param idCard 军官证号、护照号
     * @return
     */
    public static String otherIdCard(String idCard) {
        return desensitized(idCard);
    }

    /**
     * 军官证号、护照号等其他身份证件脱敏并加密
     *
     * @param idCard 军官证号、护照号
     * @return
     */
    public static SM4EncryptResult otherIdCardEncrypt(String idCard) {
        SM4EncryptResult encrypt = SM4Util.encrypt(idCard);
        String result = otherIdCard(idCard);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 军官证号、护照号等其他身份证件脱敏并加密
     *
     * @param idCard 军官证号、护照号
     * @return
     */
    public static SM4EncryptResult otherIdCardEncrypt(String encryptKey, String idCard) {
        String encrypt = SM4Util.encrypt(encryptKey, idCard);
        String result = otherIdCard(idCard);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 手机号脱敏
     *
     * @param mobilePhone 手机号
     * @return
     */
    public static String mobilePhone(String mobilePhone) {
        return cn.hutool.core.util.DesensitizedUtil.mobilePhone(mobilePhone);
    }

    /**
     * 手机号脱敏并加密
     *
     * @param mobilePhone 手机号
     * @return
     */
    public static DesensitizedResult mobilePhoneEncrypt(String mobilePhone) {
        SM4EncryptResult encrypt = SM4Util.encrypt(mobilePhone);
        String result = mobilePhone(mobilePhone);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 手机号脱敏并加密
     *
     * @param mobilePhone 手机号
     * @return
     */
    public static DesensitizedResult mobilePhoneEncrypt(String encryptKey, String mobilePhone) {
        String encrypt = SM4Util.encrypt(encryptKey, mobilePhone);
        String result = mobilePhone(mobilePhone);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 固定电话脱敏
     *
     * @param fixedPhone 固定电话
     * @return
     */
    public static String fixedPhone(String fixedPhone) {
        return cn.hutool.core.util.DesensitizedUtil.fixedPhone(fixedPhone);
    }

    /**
     * 固定电话脱敏并加密
     *
     * @param fixedPhone 固定电话
     * @return
     */
    public static DesensitizedResult fixedPhoneEncrypt(String fixedPhone) {
        SM4EncryptResult encrypt = SM4Util.encrypt(fixedPhone);
        String result = fixedPhone(fixedPhone);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 固定电话脱敏并加密
     *
     * @param fixedPhone 固定电话
     * @return
     */
    public static DesensitizedResult fixedPhoneEncrypt(String encryptKey, String fixedPhone) {
        String encrypt = SM4Util.encrypt(encryptKey, fixedPhone);
        String result = fixedPhone(fixedPhone);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 邮箱脱敏
     *
     * @param email 邮箱
     * @return
     */
    public static String email(String email) {
        if (StrUtil.isBlank(email)) {
            return StrUtil.EMPTY;
        }
        if (email.contains("@")) {
            String[] split = email.split("@");
            String mail = Convert.toStr(split[0]);
            String type = Convert.toStr(split[1]);
            if (mail.length() <= 3) {
                return "***@" + type;
            }
            if (mail.length() > 3) {
                return StrUtil.hide(mail, 3, mail.length()) + "@" + type;
            }
            return email;
        } else {
            return email;
        }

    }

    /**
     * 邮箱脱敏并加密
     *
     * @param email 邮箱
     * @return
     */
    public static DesensitizedResult emailEncrypt(String email) {
        SM4EncryptResult encrypt = SM4Util.encrypt(email);
        String result = email(email);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 邮箱脱敏并加密
     *
     * @param email 邮箱
     * @return
     */
    public static DesensitizedResult emailEncrypt(String encryptKey, String email) {
        String encrypt = SM4Util.encrypt(encryptKey, email);
        String result = email(email);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 银行卡号脱敏
     *
     * @param bankCard 银行卡号
     * @return
     */
    public static String bankCard(String bankCard) {
        return cn.hutool.core.util.DesensitizedUtil.bankCard(bankCard);
    }

    /**
     * 银行卡号脱敏并加密
     *
     * @param bankCard 银行卡号
     * @return
     */
    public static DesensitizedResult bankCardEncrypt(String bankCard) {
        SM4EncryptResult encrypt = SM4Util.encrypt(bankCard);
        String result = bankCard(bankCard);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 银行卡号脱敏并加密
     *
     * @param bankCard 银行卡号
     * @return
     */
    public static DesensitizedResult bankCardEncrypt(String encryptKey, String bankCard) {
        String encrypt = SM4Util.encrypt(encryptKey, bankCard);
        String result = bankCard(bankCard);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }

    /**
     * 其他信息脱敏
     *
     * @param info 信息
     * @return
     */
    public static String desensitized(String info) {
        if (StrUtil.isBlank(info)) {
            return StrUtil.EMPTY;
        }
        int length = info.length();
        if (length <= 2) {
            return StrUtil.hide(info, 0, info.length());
        }
        if (length <= 6) {
            return StrUtil.hide(info, 1, info.length() - 1);
        }
        int start = info.length() / 3;
        return StrUtil.hide(info, start, info.length() - start);
    }

    /**
     * 其他信息脱敏并加密
     *
     * @param info 信息
     * @return
     */
    public static DesensitizedResult desensitizedEncrypt(String info) {
        SM4EncryptResult encrypt = SM4Util.encrypt(info);
        String result = desensitized(info);
        return new DesensitizedResult(encrypt, result);
    }

    /**
     * 其他信息脱敏并加密
     *
     * @param info 信息
     * @return
     * @encryptKey 密钥
     */
    public static DesensitizedResult desensitizedEncrypt(String encryptKey, String info) {
        String encrypt = SM4Util.encrypt(encryptKey, info);
        String result = desensitized(info);
        return new DesensitizedResult(encrypt, encryptKey, result);
    }


    /**
     * 脱敏并加密
     *
     * @param info 信息
     * @return
     * @encryptKey 密钥
     */
    public static DesensitizedResult desensitizedEncrypt(String encryptKey, String info, DesensitiseTypeEnum desensitiseType) {
        String encrypt = SM4Util.encrypt(encryptKey, info);
        if (DesensitiseTypeEnum.personName.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, personName(info));
        } else if (DesensitiseTypeEnum.idCard.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, idCard(info));
        } else if (DesensitiseTypeEnum.idCardStrong.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, idCardStrong(info));
        } else if (DesensitiseTypeEnum.otherIdCard.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, otherIdCard(info));
        } else if (DesensitiseTypeEnum.mobilePhone.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, mobilePhone(info));
        } else if (DesensitiseTypeEnum.fixedPhone.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, fixedPhone(info));
        } else if (DesensitiseTypeEnum.email.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, email(info));
        } else if (DesensitiseTypeEnum.bankCard.name().equals(desensitiseType.name())) {
            return new DesensitizedResult(encrypt, encryptKey, bankCard(info));
        } else {
            return new DesensitizedResult(encrypt, encryptKey, desensitized(info));
        }

    }

    /**
     * 对信息进行脱敏
     *
     * @param info            信息
     * @param desensitiseType 脱敏类型
     * @return
     */
    public static String desensitized(String info, DesensitiseTypeEnum desensitiseType) {
        if (DesensitiseTypeEnum.personName.name().equals(desensitiseType.name())) {
            return personName(info);
        } else if (DesensitiseTypeEnum.idCard.name().equals(desensitiseType.name())) {
            return idCard(info);
        } else if (DesensitiseTypeEnum.idCardStrong.name().equals(desensitiseType.name())) {
            return idCardStrong(info);
        } else if (DesensitiseTypeEnum.otherIdCard.name().equals(desensitiseType.name())) {
            return otherIdCard(info);
        } else if (DesensitiseTypeEnum.mobilePhone.name().equals(desensitiseType.name())) {
            return mobilePhone(info);
        } else if (DesensitiseTypeEnum.fixedPhone.name().equals(desensitiseType.name())) {
            return fixedPhone(info);
        } else if (DesensitiseTypeEnum.email.name().equals(desensitiseType.name())) {
            return email(info);
        } else if (DesensitiseTypeEnum.bankCard.name().equals(desensitiseType.name())) {
            return bankCard(info);
        } else {
            return desensitized(info);
        }
    }

    private static <T> DesensitizedConfig[] getDesensitizedConfigByT(T t) {
        Field[] fields = t.getClass().getDeclaredFields();
        Map<String, List<String>> encryptFieldMap = new HashMap<>();
        Map<String, List<String>> desensitiseFieldMap = new HashMap<>();
        Map<String, List<DesensitiseTypeEnum>> desensitiseTypeMap = new HashMap<>();
        for (Field field : fields) {
            Security security = field.getAnnotation(Security.class);
            if (security != null) {
                List<String> encryptFields = encryptFieldMap.getOrDefault(security.encryptKey(), new ArrayList<>());
                List<String> desensitiseFields = desensitiseFieldMap.getOrDefault(security.encryptKey(), new ArrayList<>());
                List<DesensitiseTypeEnum> desensitiseTypes = desensitiseTypeMap.getOrDefault(security.encryptKey(), new ArrayList<>());
                encryptFields.add(field.getName());
                desensitiseFields.add(StrUtil.EMPTY.equals(security.desensitiseField()) ? StrUtil.NULL : security.desensitiseField());
                desensitiseTypes.add(security.desensitiseType());
                encryptFieldMap.put(security.encryptKey(), encryptFields);
                desensitiseFieldMap.put(security.encryptKey(), desensitiseFields);
                desensitiseTypeMap.put(security.encryptKey(), desensitiseTypes);
            }
        }
        if (encryptFieldMap.size() > 0) {
            DesensitizedConfig[] configs = new DesensitizedConfig[encryptFieldMap.keySet().size()];
            int k = 0;
            for (String key : encryptFieldMap.keySet()) {
                List<String> encryptFields = encryptFieldMap.get(key);
                List<String> desensitiseFields = desensitiseFieldMap.get(key);
                List<DesensitiseTypeEnum> desensitiseTypes = desensitiseTypeMap.get(key);
                DesensitizedConfig config = new DesensitizedConfig(key, StringUtils.join(encryptFields, ","), StringUtils.join(desensitiseFields, ","));
                config.setDesensitiseTypes(desensitiseTypes.toArray(new DesensitiseTypeEnum[desensitiseTypes.size()]));
                configs[k] = config;
                k++;
            }
            return configs;
        }
        return null;
    }

    public static <T> void desensitizedEncrypt(T t) {
        DesensitizedConfig[] desensitizedConfigs = getDesensitizedConfigByT(t);
        if (null == desensitizedConfigs) {
            Console.error("没有需要加密的字段:{}", t);
            return;
        }
        Map<String, Object> map = BeanUtil.beanToMap(t);
        Map<String, Object> result = desensitizedEncrypt(map, true, desensitizedConfigs);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            ReflectUtil.setFieldValue(t, entry.getKey(), entry.getValue());
        }
    }

    public static <T> void desensitizedDecrypt(T t) {
        DesensitizedConfig[] desensitizedConfigs = getDesensitizedConfigByT(t);
        if (null == desensitizedConfigs) {
            Console.error("没有需要解密的字段:{}", t);
            return;
        }
        Map<String, Object> map = BeanUtil.beanToMap(t);
        Map<String, Object> result = desensitizedDecrypt(map, true, desensitizedConfigs);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            ReflectUtil.setFieldValue(t, entry.getKey(), entry.getValue());
        }
    }

    public static <T> void desensitizedEncrypt(Map<String, Object> map, DesensitizedConfig... desensitizedConfig) {
        desensitizedEncrypt(map, false, desensitizedConfig);
    }

    public static <T> void desensitizedDecrypt(Map<String, Object> map, DesensitizedConfig... desensitizedConfig) {
        desensitizedDecrypt(map, false, desensitizedConfig);
    }

    private static <T> Map<String, Object> desensitizedEncrypt(Map<String, Object> map, boolean isBean, DesensitizedConfig... desensitizedConfig) {
        Map<String, Object> newData = new HashMap<>();
        for (DesensitizedConfig config : desensitizedConfig) {
            String encryptKey = config.getEncryptKey();
            if (StrUtil.isBlank(encryptKey)) {
                continue;
            }
            String key = Convert.toStr(map.get(encryptKey));
            if (StrUtil.isBlank(key)) {
                key = SM4Util.generateKey();
                newData.put(encryptKey, key);
            }
            String encryptFields = Convert.toStr(config.getEncryptFields());
            String desensitiseFields = Convert.toStr(config.getDesensitiseFields());
            DesensitiseTypeEnum[] desensitiseTypes = config.getDesensitiseTypes();
            String[] encryptFieldArr = encryptFields.split(",");
            String[] desensitiseFieldArr = desensitiseFields.split(",");
            int k = 0;
            for (String encryptField : encryptFieldArr) {
                String value = Convert.toStr(map.get(encryptField));
                if (StrUtil.isBlank(value)) {
                    continue;
                }
                DesensitizedResult desensitizedResult = desensitizedEncrypt(key, value, desensitiseTypes[k]);
                newData.put(encryptField, desensitizedResult.getEncryptData());
                String desensitiseField = desensitiseFieldArr[k];
                if (!StrUtil.isNullOrUndefined(desensitiseField)) {
                    newData.put(desensitiseField, desensitizedResult.getResult());
                }
                k++;
            }
        }
        if (isBean) {
            return newData;
        }
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static <T> Map<String, Object> desensitizedDecrypt(Map<String, Object> map, boolean isBean, DesensitizedConfig... desensitizedConfig) {
        Map<String, Object> newData = new HashMap<>();
        for (DesensitizedConfig config : desensitizedConfig) {
            String encryptKey = config.getEncryptKey();
            if (StrUtil.isBlank(encryptKey)) {
                continue;
            }
            String key = Convert.toStr(map.get(encryptKey));
            if (StrUtil.isBlank(key)) {
                continue;
            }
            String encryptFields = Convert.toStr(config.getEncryptFields());
            String[] encryptFieldArr = encryptFields.split(",");
            int k = 0;
            for (String encryptField : encryptFieldArr) {
                String value = Convert.toStr(map.get(encryptField));
                if (StrUtil.isNotBlank(value)) {
                    newData.put(encryptField, SM4Util.decrypt(key, value));
                }
                k++;
            }
        }
        if (isBean) {
            return newData;
        }
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static void main(String[] args) {
        /*System.out.println(personName("张"));
        System.out.println(personName("张三"));
        System.out.println(personName("Tony"));
        System.out.println(idCard("110101199003071559"));
        System.out.println(idCard("511702800222130"));
        System.out.println(idCardStrong("110101199003071559"));
        System.out.println(idCardStrong("511702800222130"));
        System.out.println(otherIdCard("1"));
        System.out.println(otherIdCard("12"));
        System.out.println(otherIdCard("123456"));
        System.out.println(otherIdCard("1234567"));
        System.out.println(otherIdCard("511702800222130"));
        System.out.println(mobilePhone("13800138000"));
        System.out.println(fixedPhone("82461977"));
        System.out.println(fixedPhone("010-82461977"));
        System.out.println(fixedPhone("0375-82461977"));
        System.out.println(fixedPhone("0375-8246197"));
        System.out.println(email("testtest@163.com"));
        System.out.println(email("abc@163.com"));
        System.out.println(bankCard("622575123451496"));
        System.out.println(bankCard("6225751234561496"));


        System.out.println(personNameEncrypt("张"));
        System.out.println(personNameEncrypt(SM4Util.generateKey(), "张三"));
        System.out.println(idCardEncrypt(SM4Util.generateKey(), "110101199003071559"));
        System.out.println(idCardEncrypt(SM4Util.generateKey(), "511702800222130"));
        System.out.println(idCardStrongEncrypt(SM4Util.generateKey(), "110101199003071559"));
        System.out.println(idCardStrongEncrypt(SM4Util.generateKey(), "511702800222130"));
        System.out.println(otherIdCardEncrypt(SM4Util.generateKey(), "1"));
        System.out.println(otherIdCardEncrypt(SM4Util.generateKey(), "12"));
        System.out.println(otherIdCardEncrypt(SM4Util.generateKey(), "123456"));
        System.out.println(otherIdCardEncrypt(SM4Util.generateKey(), "1234567"));
        System.out.println(otherIdCardEncrypt(SM4Util.generateKey(), "511702800222130"));
        System.out.println(mobilePhoneEncrypt(SM4Util.generateKey(), "13800138000"));
        System.out.println(fixedPhoneEncrypt(SM4Util.generateKey(), "82461977"));
        System.out.println(fixedPhoneEncrypt(SM4Util.generateKey(), "010-82461977"));
        System.out.println(fixedPhoneEncrypt(SM4Util.generateKey(), "0375-82461977"));
        System.out.println(fixedPhoneEncrypt(SM4Util.generateKey(), "0375-8246197"));
        System.out.println(emailEncrypt(SM4Util.generateKey(), "testtest@163.com"));
        System.out.println(emailEncrypt(SM4Util.generateKey(), "abc@163.com"));
        System.out.println(bankCardEncrypt(SM4Util.generateKey(), "622575123451496"));
        System.out.println(bankCardEncrypt(SM4Util.generateKey(), "6225751234561496"));
*/

        DesensitizedConfig desensitizedConfig = new DesensitizedConfig(
                "key","phone","phone_encrypt",DesensitiseTypeEnum.mobilePhone);

        Map<String, Object> map = new HashMap<>();
        map.put("phone","15650797594");
        map.put("key","d112baf8166443e7");
        System.out.println(map);
        desensitizedEncrypt(map,desensitizedConfig);
        System.out.println(map);
        desensitizedDecrypt(map,desensitizedConfig);
        System.out.println(map);

    }
}
