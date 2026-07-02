package com.jeestudio.bpm.cache.cacheUtils;

import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.contextHolder.ApplicationContextHolder;
import com.jeestudio.bpm.utils.CacheData;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.JwtUtil;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * @Description: 缓存工具
 */
@Component
public class CacheUtil {

    private static Logger logger = LoggerFactory.getLogger(CacheUtil.class);

    private static CacheI redisUtil = null;

    public CacheUtil() {
        if (redisUtil == null) {
            ProjectProperties bean = ApplicationContextHolder.getBean(ProjectProperties.class);
            if (bean != null) {
                if ("timedCacheUtil".equalsIgnoreCase(bean.getCacheImpl())) {
                    redisUtil = ApplicationContextHolder.getBean(TimedCacheUtil.class);
                } else if ("redisCacheUtil".equalsIgnoreCase(bean.getCacheImpl())) {
                    redisUtil = ApplicationContextHolder.getBean(RedisUtil.class);
                } else {
                    redisUtil = ApplicationContextHolder.getBean(RedisUtil.class);
                }
            }
        }
    }

    /**
     * Set object
     */
    public String setObjectCache(String key, String value) {
        return redisUtil.setObject(redisUtil.getCachePrefix() + key, value);
    }

    /**
     * Get object
     */
    public Object getObjectCache(String key) {
        try {
            return redisUtil.getObject(redisUtil.getCachePrefix() + key);
        } catch (Exception e) {
            logger.error("Error while getting object:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Get object
     */
    public String getObjectAuthorization(String key) {
        try {
            Object obj = redisUtil.getObject(redisUtil.getCachePrefix() + key);
            if (obj != null) {
                return obj.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while getting object:" + e.getMessage());
            return null;
        }
    }

    /**
     * Delete object
     */
    public String deleteObjectCache(String key) {
        return redisUtil.deleteObject(redisUtil.getCachePrefix() + key);
    }

    /**
     * Set object and expired minute
     */
    public String setObjectCacheExpireMinute(String key, Object value, Long time) {
        return redisUtil.setObjectExpireMinute(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Set object and expired minute
     */
    public String setObjectExpireMinuteAuthorization(CacheData cacheData) {
        return redisUtil.setObjectExpireMinute(redisUtil.getCachePrefix() + cacheData.getKey(), cacheData.getValue(), cacheData.getTimeout());
    }

    /**
     * Set object and expired hours
     */
    public String setObjectCacheExpireHour(String key, String value, Long time) {
        return redisUtil.setObjectExpireHour(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Set object and expired days
     */
    public String setObjectCacheExpireDay(String key, String value, Long time) {
        return redisUtil.setObjectExpireDay(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Set hash
     */
    public String setHashCache(String key, String hashKey, String value) {
        return redisUtil.setHash(redisUtil.getCachePrefix() + key, hashKey, value);
    }

    /**
     * Get hash
     */
    public Object getHashCache(String key, String hashKey) {
        return redisUtil.getHash(redisUtil.getCachePrefix() + key, hashKey);
    }

    /**
     * Delete hash
     */
    public String deleteHashCache(String key, String hashKey) {
        return redisUtil.deleteHash(redisUtil.getCachePrefix() + key, hashKey);
    }

    /**
     * Delete hash of matching key
     */
    public String deleteLikeHashCache(String key, String hashKey) {
        return redisUtil.deleteLikeHash(redisUtil.getCachePrefix() + key, hashKey);
    }

    /**
     * Delete hash of left matching key
     */
    public String deleteLeftLikeHashCache(String key, String hashKey) {
        return redisUtil.deleteLeftLikeHash(redisUtil.getCachePrefix() + key, hashKey);
    }

    /**
     * Set hash and expired minute
     */
    public String setHashCacheExpireMinute(String key, String hashKey, String value, Long time) {
        return redisUtil.setHashExpireMinute(redisUtil.getCachePrefix() + key, hashKey, value, time);
    }

    /**
     * Set hash and expired hours
     */
    public String setHashCacheExpireHour(String key, String hashKey, String value, Long time) {
        return redisUtil.setHashExpireHour(redisUtil.getCachePrefix() + key, hashKey, value, time);
    }

    /**
     * Set hash and expired days
     */
    public String setHashCacheExpireDay(String key, String hashKey, String value, Long time) {
        return redisUtil.setHashExpireDay(redisUtil.getCachePrefix() + key, hashKey, value, time);
    }

    /**
     * Set list
     */
    public String setListCache(String key, String value) {
        return redisUtil.setList(redisUtil.getCachePrefix() + key, value);
    }

    /**
     * Get list
     */
    public Object getListCache(String key) {
        try {
            return redisUtil.getList(redisUtil.getCachePrefix() + key);
        } catch (Exception e) {
            logger.error("Error while getting list:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete List
     */
    public String deleteListCache(String key) {
        return redisUtil.deleteList(redisUtil.getCachePrefix() + key);
    }

    /**
     * Set list and expired minute
     */
    public String setListCacheExpireMinute(String key, String value, Long time) {
        return redisUtil.setListExpireMinute(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Set list and expired hours
     */
    public String setListCacheExpireHour(String key, String value, Long time) {
        return redisUtil.setListExpireHour(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Set list and expired days
     */
    public String setListCacheExpireDay(String key, String value, Long time) {
        return redisUtil.setListExpireDay(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Delete all cache
     */
    public String deleteAllCache() {
        return redisUtil.deleteAll(redisUtil.getCachePrefix());
    }

    /**
     * Get cache size
     */
    public int sizeCache() {
        return redisUtil.size();
    }

    /**
     * Get all cache keys
     */
    public Set<String> keysCache() {
        Set<String> keys = redisUtil.keys();
        return keys;
    }

    /**
     * Get all cache values
     */
    public Collection<Object> valuesCache() {
        Collection<Object> list = redisUtil.values();
        return list;
    }

    /**
     * Get cache size of matching key
     * @param key
     * @return
     */
    public Integer sizeLike(String key) {
        return redisUtil.sizeLike(redisUtil.getCachePrefix() + key);
    }

    /**
     * Get key set of matching the key
     */
    public Set<String> keysLike(String key) {
        Set<String> keys = redisUtil.keysLike(redisUtil.getCachePrefix() + key);
        return keys;
    }

    /**
     * Get value collection of matching the key
     */
    public Collection<Object> valuesLike(String key) {
        Collection<Object> list = redisUtil.valuesLike(redisUtil.getCachePrefix() + key);
        return list;
    }

    /**
     * Determine whether the key exists
     */
    public Boolean existKey(String key) {
        return redisUtil.exist(redisUtil.getCachePrefix() + key);
    }

    /**
     * Get key expiration time
     */
    public Long expireKey(String key) {
        return redisUtil.expire(redisUtil.getCachePrefix() + key);
    }

    /**
     * Delete cache of matching key
     */
    public String deleteLike(String key) {
        return redisUtil.deleteLike(redisUtil.getCachePrefix() + key);
    }

    /**
     * Save set
     */
    public String setSet(String key, String value) {
        return redisUtil.setSet(redisUtil.getCachePrefix() + key, value);
    }

    /**
     * Save set and expired minute
     */
    public String setSetExpireMinute(String key, String value, Long time) {
        return redisUtil.setSetExpireMinute(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Save set and expired hours
     */
    public String setSetExpireHour(String key, String value, Long time) {
        return redisUtil.setSetExpireHour(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Save set and expired days
     */
    public String setSetExpireDay(String key, String value, Long time) {
        return redisUtil.setSetExpireDay(redisUtil.getCachePrefix() + key, value, time);
    }

    /**
     * Save zset
     */
    public String setZset(String key, String value, String num) {
        return redisUtil.setZset(redisUtil.getCachePrefix() + key, value, Double.valueOf(num));
    }

    /**
     * Save zset and expired minute
     */
    public String setZsetExpireMinute(String key, String value, String num, Long time) {
        return redisUtil.setZsetExpireMinute(redisUtil.getCachePrefix() + key, value, Double.valueOf(num), time);
    }

    /**
     * Save zset and expired hours
     */
    public String setZsetExpireHour(String key, String value, String num, Long time) {
        return redisUtil.setZsetExpireHour(redisUtil.getCachePrefix() + key, value, Double.valueOf(num), time);
    }

    /**
     * Save zset and expired days
     */
    public String setZsetExipreDay(String key, String value, String num, Long time) {
        return redisUtil.setZsetExipreDay(redisUtil.getCachePrefix() + key, value, Double.valueOf(num), time);
    }

    /**
     * Get hash keys
     */
    public Set<String> getHashKeys(String key) {
        Set<String> keys = redisUtil.getHashKeys(redisUtil.getCachePrefix() + key);
        return keys;
    }

    /**
     * Generate signature and save token cache
     */
    public String getTokenSetCache(String username, String userId) {
        String token = JwtUtil.sign(username, userId);
        String clientId = JwtUtil.getClaim(token, "clientId");
        if (redisUtil.getLoginModeSingle()) {
            this.deleteLike(Global.PREFIX_SHIRO_REFRESH_TOKEN + username + "_" + userId + "_token_*");
        }
        this.setObjectCacheExpireMinute(Global.PREFIX_SHIRO_REFRESH_TOKEN + username + "_" + userId + "_token_clientId_" + clientId, token, Global.EXRP_MINUTE);
        return token;
    }

    /**
     * Delete current user's token cache
     */
    public void deleteTokenCache(String token, String currentUserName, String currentUserId) {
        String clientId = JwtUtil.getClaim(token, "clientId");
        this.deleteObjectCache(Global.PREFIX_SHIRO_REFRESH_TOKEN + currentUserName + "_" + currentUserId + "_token_clientId_" + clientId);
    }
}
