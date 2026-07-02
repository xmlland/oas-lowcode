package com.jeestudio.tools.security.utils.security;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * @Description: SM2非对称加密工具
 */
public class SM2Util {
    public static AsymmetricKey generateKey() {
        SM2 sm2 = SmUtil.sm2();
        //私钥，16进制格式，自己保存，格式如a2081b5b81fbea0b6b973a3ab6dbbbc65b1164488bf22d8ae2ff0b8260f64853
        BigInteger privateKey = ((BCECPrivateKey) sm2.getPrivateKey()).getD();
        String privateKeyHex = privateKey.toString(16);
        //公钥，16进制格式，发给前端，格式如04813d4d97ad31bd9d18d785f337f683233099d5abed09cb397152d50ac28cc0ba43711960e811d90453db5f5a9518d660858a8d0c57e359a8bf83427760ebcbba
        ECPoint ecPoint = ((BCECPublicKey) sm2.getPublicKey()).getQ();
        String publicKeyHex = Hex.toHexString(ecPoint.getEncoded(false));
        return new AsymmetricKey(privateKeyHex, publicKeyHex);
    }

    public static String encrypt(String publicKey, String encryptStr) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptHex(encryptStr, KeyType.PublicKey);
    }

    public static String decrypt(String privateKey, String decryptStr) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.decryptStr(decryptStr, KeyType.PrivateKey);
    }

    public static void main(String[] args) {
        String text = "黄河";

        AsymmetricKey asymmetricKey = SM2Util.generateKey();
        String encrypt = SM2Util.encrypt(asymmetricKey.getPublicKey(), text);
        System.out.println(asymmetricKey);
        System.out.println(encrypt);
        System.out.println(SM2Util.decrypt(asymmetricKey.getPrivateKey(), encrypt));

    }
}
