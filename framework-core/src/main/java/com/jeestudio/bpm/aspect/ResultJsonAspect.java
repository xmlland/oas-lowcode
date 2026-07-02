package com.jeestudio.bpm.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.security.TranEncryptUtil;
import com.jeestudio.bpm.security.authentication.Authenticator;
import com.jeestudio.bpm.security.authentication.RequireAuthentication;
import com.jeestudio.bpm.security.authentication.RequireAuthenticationType;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import com.jeestudio.tools.security.utils.security.SM2Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

/**
 * @Description: 响应结果处理切面
 */
@Component
@Aspect
@Slf4j
public class ResultJsonAspect {

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    private SecLogService secLogService;

    private static CacheUtil cacheUtil = new CacheUtil();

    //@Value("${passwordMapFile}")
    private String passwordMapFile;

    // 定义切点Pointcut
    @Pointcut("execution(public com.jeestudio.bpm.utils.ResultJson * (..)) && @within(org.springframework.web.bind.annotation.RestController)")
    public void resultJsonMethods() {
    }

    @Pointcut("@annotation(com.jeestudio.bpm.security.authentication.RequireAuthentication)")
    public void authMethods () {

    }

    @Around("authMethods()")
    public Object auth(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求头
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        String loginName = UserUtil.getCurrentLoginName();
        String ip = oConvertUtils.getIpAddrByRequest(request);


        // 获取所需数据
        String authorizationKey = request.getHeader("authorizationKey");                // 用于校验的内容(加密后)
        String authorizationPublicKey = request.getHeader("authorizationPublicKey");    // 公钥
        String authorizationPerson = request.getHeader("authPerson");                            // 校验人

        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAuthentication annotation = method.getAnnotation(RequireAuthentication.class);
        RequireAuthenticationType requireAuthenticationType = annotation.authenticationType();

        String authTypeCode = requireAuthenticationType.getCode();
        String authTypeName = requireAuthenticationType.getName();
        String msg = "请进行二次鉴权";
        // 自定义验证器
        Authenticator authenticator = null;
        if(RequireAuthenticationType.NONE.equals(requireAuthenticationType)){
            Class<? extends Authenticator> authenticatorClass = annotation.authenticator();
            authenticator = authenticatorClass.getDeclaredConstructor().newInstance();
            authTypeName = authenticator.getAuthName();
            authTypeCode = authenticator.getAuthCode();
            msg = authenticator.getMsg();
        }

        // 查看是否有所需信息
        if(StrUtil.isEmpty(authorizationKey)|| StrUtil.isEmpty(authorizationPublicKey) || StrUtil.isEmpty(authorizationPerson)){
            LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

            //返回密钥对，返回公钥，密钥存入Redis缓存中
            AsymmetricKey asymmetricKey = SM2Util.generateKey();
            String publicKey = asymmetricKey.getPublicKey();
            String privateKey = asymmetricKey.getPrivateKey();
            cacheUtil.setHashCacheExpireMinute(Global.USER_AUTH_KEY, publicKey, privateKey, 15L);

            resultMap.put("authPublicKey", publicKey);
            resultMap.put("authTypeCode", authTypeCode);
            resultMap.put("authTypeName", authTypeName);
            return new ResultJson(ResultJson.REQUIRE_AUTHENTICATION,msg,resultMap);
        } else {
            String privateKey = cacheUtil.getHashCache(Global.USER_AUTH_KEY, authorizationPublicKey).toString();
            String authPerson = SM2Util.decrypt(privateKey, authorizationPerson);
            String authKey = SM2Util.decrypt(privateKey, authorizationKey);

            if(RequireAuthenticationType.PASSWORD.equals(requireAuthenticationType)){
                // 从密码json中进行校验
                JSONObject passwordMap;
                try {
                    String content = new String(Files.readAllBytes(Paths.get(passwordMapFile)));
                    passwordMap = JSON.parseObject(content);
                } catch (Exception e){
                    log.error("Failed to read password map file", e);
                    return new ResultJson(-1,"未配置密钥，请联系管理员",new LinkedHashMap<>());
                }

                String authKeySm3 = SmUtil.sm3(authKey);
                String authPersonSm3 = SmUtil.sm3(authPerson);
                String pwdSm3 = passwordMap.getString(authPersonSm3);
                if(authKeySm3.equals(pwdSm3)){
                    cacheUtil.deleteHashCache(Global.USER_AUTH_KEY, authorizationPublicKey);
                    secLogService.saveSecLog(loginName, ip, "二次鉴权成功，验证方式：" + authTypeName +"，验证人：" + authPerson, "二次鉴权", Global.YES);
                    return joinPoint.proceed();
                }else {
                    secLogService.saveSecLog(loginName, ip, "二次鉴权失败，验证方式：" + authTypeName +"，验证人：" + authPerson, "二次鉴权", Global.NO);
                    return new ResultJson(-1,"校验失败",new LinkedHashMap<>());
                }
            }
            // 后续扩展：根据认证类型接入更多二次鉴权验证器
            if(RequireAuthenticationType.NONE.equals(requireAuthenticationType)){
                if(authenticator.authenticate(loginName,authPerson,authorizationPublicKey, authorizationKey)){
                    // 禁止多次使用
                    cacheUtil.deleteHashCache(Global.USER_AUTH_KEY + authPerson, authorizationPublicKey);
                    secLogService.saveSecLog(loginName, ip, "二次鉴权成功，验证方式：" + authTypeName +"，验证人：" + authPerson, "二次鉴权", Global.YES);
                    return joinPoint.proceed();
                } else {
                    secLogService.saveSecLog(loginName, ip, "二次鉴权失败，验证方式：" + authTypeName +"，验证人：" + authPerson, "二次鉴权", Global.NO);
                    return new ResultJson(-1,"校验失败",new LinkedHashMap<>());
                }
            }
            return new ResultJson(-1,"未配置验证器，请联系管理员",new LinkedHashMap<>());
        }
    }


    @Around("resultJsonMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        try {
            HttpServletRequest request = ContextHolderUtil.getHttpServletRequest();
            //设置当前时间戳
            request.getSession().setAttribute("requestTimestamp", System.currentTimeMillis());
            proceed = joinPoint.proceed();
            if (ResultJson.class.equals(proceed.getClass())) {
                ResultJson resultJson = (ResultJson) proceed;
                resultJson.setPublicKey(TranEncryptUtil.getPublicKey());
                resultJson.setEncryptWhiteList(projectProperties.getEncryptWhiteList());
                if (resultJson.getToken() != null) {
                    resultJson.put("tokenEncrypt", true);
                    resultJson.setToken(TokenSecurityUtil.encrypt(resultJson.getToken()));
                }
            }
            return proceed;
        } catch (Throwable throwable) {
            Method method = getMethod(joinPoint);
            if (method != null && ResultJson.class.equals(method.getReturnType())) {
                log.error("Request processing failed: {}", ExceptionUtils.getStackTrace(throwable));
                if (throwable instanceof BusinessException) {
                    proceed = ResultJson.failed(throwable.getMessage());  // 业务异常可以返回消息
                } else {
                    proceed = ResultJson.failed("操作失败，请联系管理员");  // 系统异常不暴露细节
                }
                return proceed;
            } else if (method != null && String.class.equals(method.getReturnType())) {
                log.error("Request processing failed: {}", ExceptionUtils.getStackTrace(throwable));
                return null;
            } else {
                throw throwable;
            }
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        Class<?> targetClass = null;
        try {
            targetClass = Class.forName(targetName);
            Method[] methods = targetClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Failed to get method from join point", e);
        }
        return null;
    }
}
