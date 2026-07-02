package com.jeestudio.tools.security.config;

import com.jeestudio.tools.base.enums.DesensitiseTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @Description: 脱敏配置
 */
public class DesensitizedConfig {

    /**
     * 加密密钥字段
     */
    private String encryptKey;

    /**
     * 需加密字段 ,分隔
     */
    private String encryptFields;
    /**
     * 脱敏数据存储字段 ,分隔
     */
    private String desensitiseFields;

    /**
     * 加密类型
     */
    private DesensitiseTypeEnum[] desensitiseTypes;


    public DesensitizedConfig(String encryptKey, String encryptFields, String desensitiseFields) {
        this.encryptKey = encryptKey;
        this.encryptFields = encryptFields;
        this.desensitiseFields = desensitiseFields;
    }

    public DesensitizedConfig(String encryptKey, String encryptFields, String desensitiseFields, DesensitiseTypeEnum... desensitiseTypes) {
        this.encryptKey = encryptKey;
        this.encryptFields = encryptFields;
        this.desensitiseFields = desensitiseFields;
        this.desensitiseTypes = desensitiseTypes;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public String getEncryptFields() {
        return encryptFields;
    }

    public void setEncryptFields(String encryptFields) {
        this.encryptFields = encryptFields;
    }

    public String getDesensitiseFields() {
        return desensitiseFields;
    }

    public void setDesensitiseFields(String desensitiseFields) {
        this.desensitiseFields = desensitiseFields;
    }

    public DesensitiseTypeEnum[] getDesensitiseTypes() {
        return desensitiseTypes;
    }

    public void setDesensitiseTypes(DesensitiseTypeEnum[] desensitiseTypes) {
        this.desensitiseTypes = desensitiseTypes;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("encryptKey", encryptKey)
                .append("encryptFields", encryptFields)
                .append("desensitiseFields", desensitiseFields)
                .append("desensitiseTypes", desensitiseTypes)
                .toString();
    }
}
