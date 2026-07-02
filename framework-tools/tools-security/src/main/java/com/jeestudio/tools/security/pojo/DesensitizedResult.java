package com.jeestudio.tools.security.pojo;

/**
 * @Description: 脱敏结果
 */
public class DesensitizedResult extends SM4EncryptResult {

    private String result;


    public DesensitizedResult(String encryptData, String encryptKey, String result) {
        super(encryptData, encryptKey);
        this.result = result;
    }

    public DesensitizedResult(SM4EncryptResult encryptResult, String result) {
        super(encryptResult.getEncryptData(), encryptResult.getEncryptKey());
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SM4EncryptResult{");
        sb.append("encryptData='").append(getEncryptData()).append('\'');
        sb.append(", encryptKey='").append(getEncryptKey()).append('\'');
        sb.append(", result='").append(getResult()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
