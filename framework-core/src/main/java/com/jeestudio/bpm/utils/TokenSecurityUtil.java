package com.jeestudio.bpm.utils;

import com.jeestudio.tools.security.utils.security.SM2Util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Description: Token安全工具
 */
public class TokenSecurityUtil {

    public static String encrypt(String token) {
        HttpServletRequest httpServletRequest = ContextHolderUtil.getHttpServletRequest();
        String publicKey = httpServletRequest.getHeader("tokenPublicKey");
        if (publicKey == null) {
            return token;
        }
        return SM2Util.encrypt(publicKey, token);
    }
}
