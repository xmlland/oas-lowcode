package com.jeestudio.tools.security.pojo;

/**
 * @Description: SM4加密结果
 */
public class SM4EncryptResult {
    /**
     * 加密数据
     */
    private String encryptData;
    /**
     * 密钥
     */
    private String encryptKey;
    public SM4EncryptResult() {
    }
    public SM4EncryptResult(String encryptData, String encryptKey) {
        this.encryptData = encryptData;
        this.encryptKey = encryptKey;
    }

    public String getEncryptData() {
        return encryptData;
    }

    public void setEncryptData(String encryptData) {
        this.encryptData = encryptData;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SM4EncryptResult{");
        sb.append("encryptData='").append(encryptData).append('\'');
        sb.append(", encryptKey='").append(encryptKey).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
