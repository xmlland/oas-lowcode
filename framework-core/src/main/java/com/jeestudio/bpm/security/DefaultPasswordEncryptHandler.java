package com.jeestudio.bpm.security;

import com.jeestudio.bpm.utils.Digest;
import com.jeestudio.bpm.utils.Encodes;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;

/**
 * @Description: 默认密码加密处理器
 */
public class DefaultPasswordEncryptHandler extends PasswordEncryptHandler {
    public static final int HASH_INTERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    @Override
    public String encrypt(String password, String encryptPassword, String loginName) {
        byte[] salt = null;
        if (StringUtil.isNotEmpty(encryptPassword)) {
            salt = Encodes.decodeHex(ConvertUtil.getString(encryptPassword).substring(0, 16));
        } else {
            salt = Digest.generateSalt(SALT_SIZE);
        }
        byte[] hashPassword = Digest.sha1(password.getBytes(), salt,
                HASH_INTERATIONS);
        return Encodes.encodeHex(salt) + Encodes.encodeHex(hashPassword);
    }
}
