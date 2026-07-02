package com.jeestudio.bpm.security.authentication;

/**
 * @Description: 二次鉴权器接口
 */
public interface Authenticator {
    String getAuthName();
    String getAuthCode();
    default String getMsg(){
        return "请进行二次鉴权";
    };
    boolean authenticate(String loginName,String authPerson,String publicKey, String key);
}
