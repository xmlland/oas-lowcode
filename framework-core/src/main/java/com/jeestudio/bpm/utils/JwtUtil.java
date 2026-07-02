package com.jeestudio.bpm.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Description: JWT工具
 */
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 服务端JWT签名密钥，由外部配置注入，不可从token反推
     */
    private static volatile String jwtSecretKey;

    /**
     * 初始化JWT密钥，由Spring容器启动时调用
     * 密钥未配置或使用默认值时拒绝初始化，防止不安全降级
     */
    public static void init(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "JWT密钥未配置！请在环境变量 JWT_SECRET_KEY 或配置 project.jwt-secret-key 中设置至少32字符的强随机密钥");
        }
        if (secretKey.startsWith("please-change")) {
            throw new IllegalArgumentException(
                    "JWT密钥使用了不安全默认值，请更换为强随机密钥（至少32字符）");
        }
        if (secretKey.length() < 32) {
            logger.warn("JWT密钥长度不足32字符，建议使用更长的强随机密钥");
        }
        jwtSecretKey = secretKey;
        logger.info("JWT签名密钥已成功初始化");
    }

    /**
     * 生成HMAC签名密钥：使用服务端密钥 + 用户信息混合，确保不可逆向
     * 密钥未初始化时抛出异常，彻底禁止不安全的降级签名
     */
    private static String buildSecret(String userName, String userId) throws UnsupportedEncodingException {
        if (jwtSecretKey == null || jwtSecretKey.isEmpty()) {
            throw new IllegalStateException(
                    "JWT签名密钥未初始化，无法签发或验证Token。请检查 JWT_SECRET_KEY 环境变量配置");
        }
        return Base64ConvertUtil.encode(jwtSecretKey + ":" + userName + ":" + userId);
    }

    /**
     * The information in token can be obtained without secret decryption
     */
    public static String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            logger.error("Error while decoding token, " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * Decrypt Token
     *
     * @param userName
     * @return
     */
    public static Map<String, Object> verify(String userName, String userId, String token) {
        String clientId = getClaim(token, "clientId");

        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String secret = buildSecret(userName, userId);
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withClaim("username", userName)
                    .withClaim("userId", userId)
                    .withClaim("clientId", clientId)
                    .build();
            DecodedJWT jwt = null;
            if (verifier != null) {
                jwt = verifier.verify(token);
            }
            map.put("isSuccess", true);
            map.put("exception", null);
        } catch (Exception ex) {
            logger.error("JWTToken Expire");
            map.put("isSuccess", false);
            map.put("exception", ex);
        }
        return map;
    }

    /**
     * Generate signature
     *
     * @param userName
     * @param userId
     * @return
     */
    public static String sign(String userName, String userId) {
        try {
            String secret = buildSecret(userName, userId);
            Date current = new Date();
            Date date = new Date(System.currentTimeMillis() + (Global.EXRP_MINUTE / 2) * 60 * 1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Map<String, Object> map = new HashMap<>();
            map.put("alg", "HS256");
            map.put("typ", "JWT");
            return JWT.create().withHeader(map)
                    .withClaim("username", userName)
                    .withClaim("userId", userId)
                    .withClaim("clientId", UUID.randomUUID().toString().replaceAll("-", ""))
                    .withClaim("iss", "Service")
                    .withClaim("aud", "APP")
                    .withIssuedAt(current)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while signing, " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * Get current user by token(username_userid)
     */
    public static String getCurrentUser(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            String username = jwt.getClaim(Global.USERNAME).asString();
            String userId = jwt.getClaim(Global.USERID).asString();
            return username + "_" + userId;
        } catch (JWTDecodeException e) {
            logger.error("Error while getting current user by token, " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}