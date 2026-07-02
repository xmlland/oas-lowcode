package com.jeestudio.tools.auth;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;

import java.util.Calendar;
import java.util.Date;

/**
 * @Description: JWT工具
 */
public class JwtUtil {

    private static String tokenPrefix = "";
    private static String secret = "";

    private static Integer expireHour = 24;

    /**
     * @param secret 密钥
     */
    public static void init(String secret) {
        JwtUtil.secret = secret;
    }

    /**
     * @param tokenPrefix token 额外前缀
     * @param secret      密钥
     */
    public static void init(String tokenPrefix, String secret) {
        JwtUtil.tokenPrefix = tokenPrefix;
        JwtUtil.secret = secret;
    }

    /**
     * @param tokenPrefix token 额外前缀
     * @param secret      密钥
     * @param expireHour  过期时间
     */
    public static void init(String tokenPrefix, String secret, Integer expireHour) {
        JwtUtil.tokenPrefix = tokenPrefix;
        JwtUtil.secret = secret;
        JwtUtil.expireHour = expireHour;
    }

    /**
     * 根据用户信息生成token
     *
     * @param userInfo 用户信息json
     * @return
     */
    public static String sign(JSONObject userInfo) {

        Calendar calendar = CalendarUtil.calendar();
        calendar.add(Calendar.HOUR, expireHour);
        Date date = calendar.getTime();
        return sign(userInfo, date);
    }


    /**
     * 根据用户信息生成token
     *
     * @param userInfo 用户信息json
     * @param expDate  过期时间
     * @return
     */
    public static String sign(JSONObject userInfo, Date expDate) {
        return sign(userInfo, new Date(), expDate);
    }

    /**
     * 根据用户信息生成token
     *
     * @param userInfo 用户信息json
     * @param notBefore  生效时间
     * @param expDate  过期时间
     * @return
     */
    public static String sign(JSONObject userInfo, Date notBefore, Date expDate) {
        if (StrUtil.isEmpty(secret)) {
            throw new BusinessException("请先初始化");
        }
        String token = JWT.create()
                .setKey(secret.getBytes())
                .setPayload("userInfo", userInfo)
                .setIssuedAt(new Date())
                .setNotBefore(notBefore)
                .setExpiresAt(expDate)
                .sign();
        return tokenPrefix + token;
    }

    /**
     * 验证token是否有效
     *
     * @param token
     * @return
     */
    public static boolean verify(String token) {
        if (StrUtil.isEmpty(secret)) {
            throw new BusinessException("请先初始化");
        }
        token = token.replaceAll("(?i)" + tokenPrefix, "");
        try {
            JWTValidator jwtValidator = JWTValidator.of(token).validateAlgorithm(JWTSignerUtil.hs256(secret.getBytes()));
            jwtValidator.validateDate();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return true;
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @return
     */
    public static JSONObject getUserInfo(String token) {
        if (!verify(token)) {
            throw new BusinessException("token已失效");
        }
        token = token.replaceAll("(?i)" + tokenPrefix, "");
        JWT jwt = JWT.of(token);
        JWTPayload jwtPayload = jwt.getPayload();
        return jwtPayload.getClaimsJson().getJSONObject("userInfo");

    }


    public static void main(String[] args) {
        init("zry");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 1);
        jsonObject.put("name", "zry");
        String token = sign(jsonObject);
        System.out.println(token);
        System.out.println(getUserInfo(token));
    }
}
