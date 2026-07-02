package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.utils.AuthUtil;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Description: 动态口令认证服务
 */
@Service
public class AuthService {
    protected static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static CacheUtil cacheUtil = new CacheUtil();

    private String getSecretKey() {
        return StringUtil.getString(cacheUtil.getObjectCache(AuthUtil.SECRET_KEY));
    }

    public boolean validateCode(String code) {
        long timestamp = System.currentTimeMillis();
        String expectedCode = AuthUtil.generateCode(this.getSecretKey(), timestamp);
        return expectedCode != null && expectedCode.equals(code);
    }

    public void setSecretKey(String secretKey) {
        cacheUtil.setObjectCache(AuthUtil.SECRET_KEY, secretKey);
    }
}
