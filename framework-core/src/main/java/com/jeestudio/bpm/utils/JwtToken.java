package com.jeestudio.bpm.utils;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @Description: JWT令牌
 */
public class JwtToken implements AuthenticationToken {

    /** JWT 原始令牌字符串。 */
    private String token;

    public JwtToken(String token){
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
