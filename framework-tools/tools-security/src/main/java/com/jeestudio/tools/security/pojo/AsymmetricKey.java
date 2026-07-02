package com.jeestudio.tools.security.pojo;

/**
 * @Description: 非对称加密密钥
 */
public class AsymmetricKey {

    private static final long serialVersionUID = 7034707362331886022L;
    private String privateKey;
    private String publicKey;

    public AsymmetricKey(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
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
