package com.jeestudio.bpm.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.contextHolder.ApplicationContextHolder;
import com.jeestudio.bpm.utils.ContextHolderUtil;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.JwtUtil;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import com.jeestudio.tools.security.utils.security.SM2Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 传输加密工具
 */
public class TranEncryptUtil {
    private static CacheUtil cacheUtil = new CacheUtil();

    private static boolean containsPattern(String input, String pattern) {
        Pattern regexPattern = Pattern.compile(pattern.replace("*",".*")+"$");
        Matcher matcher = regexPattern.matcher(input);
        return matcher.find();
    }

    public static boolean isSkip() {
        ProjectProperties bean = ApplicationContextHolder.getBean(ProjectProperties.class);
        HttpServletRequest httpServletRequest = ContextHolderUtil.getHttpServletRequest();
        assert bean != null;
        for (String url : bean.getEncryptWhiteList()) {
            if (TranEncryptUtil.containsPattern(httpServletRequest.getServletPath(), url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置公钥
     *
     * @param userId
     */
    public static void setPublicKey(String userId) {
        ProjectProperties bean = ApplicationContextHolder.getBean(ProjectProperties.class);
        if (!bean.isTransmittalEncryption()) {
            return;
        }
        HttpServletResponse response = ContextHolderUtil.getHttpServletResponse();
        String privateKey = ConvertUtil.getString(cacheUtil.getObjectCache(Global.SM2KEY_CACHE + userId));
        if (StringUtil.isNotEmpty(privateKey)) {
            String publicKey = ConvertUtil.getString(cacheUtil.getObjectCache(Global.SM2KEY_CACHE + "public:" + userId));
            response.setHeader("publicKey", publicKey);
            return;
        }
        AsymmetricKey sm2Key = SM2Util.generateKey();
        cacheUtil.setObjectCacheExpireDay(Global.SM2KEY_CACHE + userId, sm2Key.getPrivateKey(), 1L);
        cacheUtil.setObjectCacheExpireDay(Global.SM2KEY_CACHE + "public:" + userId, sm2Key.getPublicKey(), 1L);
        response.setHeader("publicKey", sm2Key.getPublicKey());
    }


    public static String getPublicKey() {
        HttpServletResponse response = ContextHolderUtil.getHttpServletResponse();
        return response.getHeader("publicKey");
    }


    public static String decryptRequestData(String data) {
        if (isSkip() || StringUtil.isEmpty(data)) {
            return data;
        }
        HttpServletRequest httpServletRequest = ContextHolderUtil.getHttpServletRequest();
        String publicKey = httpServletRequest.getHeader("serverPublicKey");
        if (StringUtil.isEmpty(publicKey)) {
            throw new BusinessException("serverPublicKey is null");
        }
        String token = JwtUtil.getCurrentUser(httpServletRequest.getHeader("token"));
        if (StringUtil.isEmpty(token)) {
            return data;
        }
        String userId = token.split("_")[1];
        String privateKey = ConvertUtil.getString(cacheUtil.getObjectCache(Global.SM2KEY_CACHE + userId));
        if (StringUtil.isEmpty(privateKey)) {
            return data;
        }
        return SM2Util.decrypt(privateKey, data);
    }

    public static Object encryptResponseData(Object object) {
        if (isSkip() || object == null) {
            return object;
        }
        HttpServletRequest httpServletRequest = ContextHolderUtil.getHttpServletRequest();
        String publicKey = httpServletRequest.getHeader("clientPublicKey");
        if (StringUtil.isEmpty(publicKey)) {
            throw new BusinessException("clientPublicKey is null");
        }
        JSONObject result = new JSONObject();
        String jsonStr = JSON.toJSONStringWithDateFormat(object, "yyyy-MM-dd HH:mm:ss",
                SerializerFeature.DisableCircularReferenceDetect);
        result.put("data", SM2Util.encrypt(publicKey, jsonStr));
        return result;
    }
}
