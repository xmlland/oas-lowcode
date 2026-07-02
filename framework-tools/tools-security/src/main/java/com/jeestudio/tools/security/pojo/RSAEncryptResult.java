package com.jeestudio.tools.security.pojo;

/**
 * @Description: RSA加密结果
 */
public class RSAEncryptResult {
    /**
     * 加密数据
     */
    private String encryptData;
    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 公钥
     */
    private String publicKey;


    public RSAEncryptResult(String encryptData, String privateKey, String publicKey) {
        this.encryptData = encryptData;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getEncryptData() {
        return encryptData;
    }

    public void setEncryptData(String encryptData) {
        this.encryptData = encryptData;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
