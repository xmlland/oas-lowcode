package com.jeestudio.bpm.authorization.CacheManager;

import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.JwtUtil;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Description: Shiro Redis缓存
 */
@Component
public class RedisCache<K,V> implements Cache<K,V> {

    private static CacheUtil cacheUtil = new CacheUtil();

    private String getPrefix(K key) {
        return Global.PREFIX_SHIRO_CACHE + JwtUtil.getCurrentUser(key.toString());
    }

    @Override
    public V get(K k) throws CacheException {
        Object obj = cacheUtil.getObjectCache(this.getPrefix(k));
        SimpleAuthorizationInfo simpleAuthorizationInfo = null;
        if (obj != null) {
            simpleAuthorizationInfo = (SimpleAuthorizationInfo)obj;
        }
        return (V) simpleAuthorizationInfo;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        return (V) cacheUtil.setObjectCacheExpireMinute(this.getPrefix(k), v, Global.EXRP_MINUTE);
    }

    @Override
    public V remove(K k) throws CacheException {
        return (V) cacheUtil.deleteObjectCache(this.getPrefix(k));
    }

    @Override
    public void clear() throws CacheException {
        cacheUtil.deleteAllCache();
    }

    @Override
    public int size() {
        Integer num = cacheUtil.sizeLike(Global.PREFIX_SHIRO_CACHE + "*");
        return num == null ? 1 : num;
    }

    @Override
    public Set<K> keys() {
        Set<String> keys = cacheUtil.keysLike(Global.PREFIX_SHIRO_CACHE + "*");
        return (Set<K>) keys;
    }

    @Override
    public Collection<V> values() {
        Collection<Object> values = cacheUtil.valuesLike(Global.PREFIX_SHIRO_CACHE + "*");
        Collection<V> collection = (Collection<V>) values;
        return collection;
    }
}
