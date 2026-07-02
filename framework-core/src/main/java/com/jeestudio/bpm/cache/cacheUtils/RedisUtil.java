package com.jeestudio.bpm.cache.cacheUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Redis缓存工具
 */
@Component("redisCacheUtil")
public class RedisUtil implements CacheI{

    private Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HashOperations hashOperations;

    @Autowired
    private ListOperations listOperations;

    @Autowired
    private SetOperations setOperations;

    @Autowired
    private ZSetOperations zSetOperations;

    @Value("${cachePrefix}")
    private String cachePrefix;

    @Value("${login.mode.single}")
    private Boolean loginModeSingle;

    public String getCachePrefix() {
        return this.cachePrefix;
    }

    public Boolean getLoginModeSingle() {
        return this.loginModeSingle;
    }

    /**
     * Determine whether the key exists
     */
    public Boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Get key expiration time
     */
    public Long expire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.MINUTES);
    }

    /**
     * Get cache size - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public int size() {
        return scanKeys("*").size();
    }

    /**
     * Get all keys - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public Set<String> keys() {
        return scanKeys("*");
    }

    /**
     * Get all values - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public Collection<Object> values() {
        Set<String> keys = scanKeys("*");
        Iterator<String> iterable = keys.iterator();
        List<Object> list = new ArrayList<Object>();
        while (iterable.hasNext()) {
            String key = iterable.next();
            DataType dt = redisTemplate.type(key);
            if ("string".equals(dt.code()) || "STRING".equals(dt.name())) {
                list.add(redisTemplate.opsForValue().get(key));
            }
        }
        return list;
    }

    /**
     * Get size of matching key - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public Integer sizeLike(String key) {
        return scanKeys(key).size();
    }

    /**
     * Get key set of matching key - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public Set<String> keysLike(String key) {
        return scanKeys(key);
    }

    /**
     * Get value collection of matching key - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public Collection<Object> valuesLike(String key) {
        Set<String> keys = scanKeys(key);
        Iterator<String> iterable = keys.iterator();
        List<Object> list = new ArrayList<Object>();
        while (iterable.hasNext()) {
            String keyIt = iterable.next();
            DataType dt = redisTemplate.type(keyIt);
            if ("string".equals(dt.code()) || "STRING".equals(dt.name())) {
                list.add(redisTemplate.opsForValue().get(keyIt));
            }
        }
        return list;
    }

    /**
     * 使用SCAN命令替代KEYS，避免阻塞Redis生产环境
     * @param pattern 匹配的键模式
     * @return 匹配的键集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> result = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        } catch (Exception e) {
            logger.error("Error while scanning keys with pattern: " + pattern, e);
        }
        return result;
    }

    /**
     * Set object
     */
    public String setObject(String key, Object object) {
        try {
            redisTemplate.opsForValue().set(key, object);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting object:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Get object
     */
    public Object getObject(String key) {
        if (this.exist(key)) {
            return redisTemplate.opsForValue().get(key);
        } else {
            return null;
        }
    }

    /**
     * Delete object
     */
    public String deleteObject(String key) {
        try {
            redisTemplate.delete(key);
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting object:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set object and expired minute
     */
    public String setObjectExpireMinute(String key, Object object, Long timeout) {
        try {
            redisTemplate.opsForValue().set(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting object and expired minute:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set object and expired hours
     */
    public String setObjectExpireHour(String key, Object object, Long timeout) {
        try {
            redisTemplate.opsForValue().set(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting object and expired hours:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set object and expired days
     */
    public String setObjectExpireDay(String key, Object object, Long timeout) {
        try {
            redisTemplate.opsForValue().set(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.DAYS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting object and expired days:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Get hash keys
     */
    public Set<String> getHashKeys(String key) {
        return hashOperations.keys(key);
    }

    /**
     * Set hash
     */
    public String setHash(String key, String hashKey, Object object) {
        try {
            hashOperations.put(key, hashKey, object);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting hash:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Get hash
     */
    public Object getHash(String key, String hashKey) {
        try {
            if (this.exist(key)) {
                if (hashOperations.hasKey(key, hashKey)) {
                    return hashOperations.get(key, hashKey);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while getting hash:" + e.getMessage());
            return null;
        }
    }

    /**
     * Delete hash
     */
    public String deleteHash(String key, String hashKey) {
        try {
            hashOperations.delete(key, hashKey);
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting hash:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set hash and expired minute
     */
    public String setHashExpireMinute(String key, String hashKey, Object object, Long timeout) {
        try {
            hashOperations.put(key, hashKey, object);
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting hash and expired minute:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set hash and expired hours
     */
    public String setHashExpireHour(String key, String hashKey, Object object, Long timeout) {
        try {
            hashOperations.put(key, hashKey, object);
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting hash and expired hours:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set hash and expired days
     */
    public String setHashExpireDay(String key, String hashKey, Object object, Long timeout) {
        try {
            hashOperations.put(key, hashKey, object);
            redisTemplate.expire(key, timeout, TimeUnit.DAYS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting hash and expired days:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set list
     */
    public String setList(String key, Object object) {
        try {
            listOperations.leftPush(key, object);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting list:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Get list
     */
    public Object getList(String key) {
        try {
            if (this.exist(key)) {
                return listOperations.range(key, 0, listOperations.size(key));
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while getting list:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete List
     */
    public String deleteList(String key) {
        try {
            listOperations.leftPop(key);
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting list:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set list and expired minute
     */
    public String setListExpireMinute(String key, Object object, Long timeout) {
        try {
            listOperations.leftPush(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting list and expired minute:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set list and expired hours
     */
    public String setListExpireHour(String key, Object object, Long timeout) {
        try {
            listOperations.leftPush(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting list and expired hours:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Set list and expired days
     */
    public String setListExpireDay(String key, Object object, Long timeout) {
        try {
            listOperations.leftPush(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.DAYS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while setting list and expired days:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete all of matching prefix - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public String deleteAll(String prefix) {
        try {
            Set<String> keys = scanKeys(prefix + "*");
            redisTemplate.delete(keys);
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting all of matching prefix:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete cache of matching key - 使用SCAN替代KEYS，避免阻塞Redis
     */
    public String deleteLike(String key) {
        try {
            Set<String> keys = scanKeys(key);
            redisTemplate.delete(keys);
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting cache of matching key:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save set
     */
    public String setSet(String key, Object object) {
        try {
            setOperations.add(key, object);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving set:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save set and expired minute
     */
    public String setSetExpireMinute(String key, Object object, Long timeout) {
        try {
            setOperations.add(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving set and expired minute:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save set and expired hours
     */
    public String setSetExpireHour(String key, Object object, Long timeout) {
        try {
            setOperations.add(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving set and expired hours:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save set and expired days
     */
    public String setSetExpireDay(String key, Object object, Long timeout) {
        try {
            setOperations.add(key, object);
            redisTemplate.expire(key, timeout, TimeUnit.DAYS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving set and expired days:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save zset
     */
    public String setZset(String key, Object object, double num) {
        try {
            zSetOperations.add(key, object, num);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving zset:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save zset and expired minute
     */
    public String setZsetExpireMinute(String key, Object object, double num, Long timeout) {
        try {
            zSetOperations.add(key, object, num);
            redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving zset and expired minute:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save zset and expired hours
     */
    public String setZsetExpireHour(String key, Object object, double num, Long timeout) {
        try {
            zSetOperations.add(key, object, num);
            redisTemplate.expire(key, timeout, TimeUnit.HOURS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving zset and expired hours:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Save zset and expired days
     */
    public String setZsetExipreDay(String key, Object object, double num, Long timeout) {
        try {
            zSetOperations.add(key, object, num);
            redisTemplate.expire(key, timeout, TimeUnit.DAYS);
            return "true";
        } catch (Exception e) {
            logger.error("Error while saving zset and expired days:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete hash of matching key
     */
    public String deleteLikeHash(String key, String hashKey) {
        try {
            Set<String> keys = hashOperations.keys(key);
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                String k = iterator.next();
                if (k.contains(hashKey)) {
                    hashOperations.delete(key, k);
                }
            }
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting hash of matching key:" + e.getMessage());
            return "false";
        }
    }

    /**
     * Delete hash of left matching key
     */
    public String deleteLeftLikeHash(String key, String hashKey) {
        try {
            Set<String> keys = hashOperations.keys(key);
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                String k = iterator.next();
                if (k.startsWith(hashKey)) {
                    hashOperations.delete(key, k);
                }
            }
            return "true";
        } catch (Exception e) {
            logger.error("Error while deleting hash of left matching key:" + e.getMessage());
            return "false";
        }
    }
}
