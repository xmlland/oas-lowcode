package com.jeestudio.bpm.cache.cacheUtils;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.jeestudio.bpm.utils.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @Description: 定时缓存工具
 */
@Component("timedCacheUtil")
@Slf4j
public class TimedCacheUtil implements CacheI {
    @Value("${cachePrefix}")
    private String cachePrefix;

    @Value("${login.mode.single}")
    private Boolean loginModeSingle;

    static int timeout = 300 * 60 * 1000; //默认300分钟过期
    static int delay = 5 * 1000; //5秒清理一次

    static TimedCache<String, Object> cacheMap = CacheUtil.newTimedCache(timeout);
    static TimedCache<String, ConcurrentHashMap<String, Object>> hashOperations = CacheUtil.newTimedCache(timeout);
    static TimedCache<String, CopyOnWriteArrayList<Object>> listOperations = CacheUtil.newTimedCache(timeout);
    static TimedCache<String, ConcurrentHashSet<Object>> setOperations = CacheUtil.newTimedCache(timeout);
    static {
        cacheMap.schedulePrune(delay);
        hashOperations.schedulePrune(delay);
        listOperations.schedulePrune(delay);
        setOperations.schedulePrune(delay);
    }

    @Override
    public String getCachePrefix() {
        return this.cachePrefix;
    }

    @Override
    public Boolean getLoginModeSingle() {
        return this.loginModeSingle;
    }

    @Override
    public Boolean exist(String key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public Long expire(String key) {
        TimedCache[] timedCaches = {cacheMap, hashOperations, listOperations, setOperations};
        for (TimedCache timedCache : timedCaches) {
            Iterator<CacheObj<String, Object>> cacheObjIterator = timedCache.cacheObjIterator();
            while (cacheObjIterator.hasNext()) {
                CacheObj<String, Object> cacheObj = cacheObjIterator.next();
                if (cacheObj.getKey().equals(key)) {
                    long l = cacheObj.getExpiredTime().getTime() - System.currentTimeMillis();
                    log.debug("expire key:{},time:{}", key, l);
                    return l;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        int size = cacheMap.size() + hashOperations.size() + listOperations.size() + setOperations.size();
        log.debug("size:{}", size);
        return size;
    }

    @Override
    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        keys.addAll(cacheMap.keySet());
        keys.addAll(hashOperations.keySet());
        keys.addAll(listOperations.keySet());
        keys.addAll(setOperations.keySet());
        return keys;
    }

    @Override
    public Collection<Object> values() {
        Collection<Object> values = new ArrayList<>();
        TimedCache[] timedCaches = {cacheMap, hashOperations, listOperations, setOperations};
        for (TimedCache timedCache : timedCaches) {
            Iterator<CacheObj<String, Object>> cacheObjIterator = timedCache.cacheObjIterator();
            while (cacheObjIterator.hasNext()) {
                CacheObj<String, Object> cacheObj = cacheObjIterator.next();
                values.add(cacheObj.getValue());
            }
        }
        return values;
    }

    @Override
    public Integer sizeLike(String key) {
        int count = 0;
        count += Math.toIntExact(cacheMap.keySet().stream().filter(k -> k.contains(key)).count());
        count += Math.toIntExact(hashOperations.keySet().stream().filter(k -> k.contains(key)).count());
        count += Math.toIntExact(listOperations.keySet().stream().filter(k -> k.contains(key)).count());
        count += Math.toIntExact(setOperations.keySet().stream().filter(k -> k.contains(key)).count());
        log.debug("sizeLike key:{},size:{}", key, count);
        return count;
    }

    @Override
    public Set<String> keysLike(String key) {
        Set<String> keys = new HashSet<>();
        keys.addAll(cacheMap.keySet().stream().filter(k -> k.contains(key)).collect(Collectors.toSet()));
        keys.addAll(hashOperations.keySet().stream().filter(k -> k.contains(key)).collect(Collectors.toSet()));
        keys.addAll(listOperations.keySet().stream().filter(k -> k.contains(key)).collect(Collectors.toSet()));
        keys.addAll(setOperations.keySet().stream().filter(k -> k.contains(key)).collect(Collectors.toSet()));
        return keys;
    }

    @Override
    public Collection<Object> valuesLike(String key) {
        Collection<Object> values = new ArrayList<>();
        values.addAll(cacheMap.keySet().stream().filter(k -> k.contains(key)).map(k -> cacheMap.get(k, false)).collect(Collectors.toList()));
        values.addAll(hashOperations.keySet().stream().filter(k -> k.contains(key)).map(k -> hashOperations.get(k, false)).collect(Collectors.toList()));
        values.addAll(listOperations.keySet().stream().filter(k -> k.contains(key)).map(k -> listOperations.get(k, false)).collect(Collectors.toList()));
        values.addAll(setOperations.keySet().stream().filter(k -> k.contains(key)).map(k -> setOperations.get(k, false)).collect(Collectors.toList()));
        return values;
    }

    @Override
    public String setObject(String key, Object object) {
        log.debug("setObject key:{}", key);
        cacheMap.put(key, object);
        return "true";
    }

    @Override
    public Object getObject(String key) {
        if (this.exist(key)) {
            Object o = cacheMap.get(key, false);
            log.debug("getObject success key:{}", key);
            return o;
        }
        return null;
    }

    @Override
    public String deleteObject(String key) {
        cacheMap.remove(key);
        log.debug("deleteObject success key:{}", key);
        return "true";
    }

    @Override
    public String setObjectExpireMinute(String key, Object object, Long timeout) {
        log.debug("setObjectExpireMinute key:{},timeout:{}", key, timeout);
        cacheMap.put(key, object, timeout * 60 * 1000);
        return null;
    }

    @Override
    public String setObjectExpireHour(String key, Object object, Long timeout) {
        log.debug("setObjectExpireHour key:{},timeout:{}", key, timeout);
        cacheMap.put(key, object, timeout * 60 * 60 * 1000);
        return null;
    }

    @Override
    public String setObjectExpireDay(String key, Object object, Long timeout) {
        log.debug("setObjectExpireDay key:{},timeout:{}", key, timeout);
        cacheMap.put(key, object, timeout * 24 * 60 * 60 * 1000);
        return null;
    }

    @Override
    public Set<String> getHashKeys(String key) {
        return hashOperations.get(key, false).keySet();
    }

    @Override
    public String setHash(String key, String hashKey, Object object) {
        Lock lock = LockUtil.getLock("timedCache-setHash-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                map.put(hashKey, object);
                hashOperations.put(key, map);
            } else {
                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(hashKey, object);
                hashOperations.put(key, map);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setHash key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public Object getHash(String key, String hashKey) {
        if (hashOperations.containsKey(key)) {
            Map<String, Object> map = hashOperations.get(key, false);
            Object o = map.get(hashKey);
            log.debug("getHash success key:{},hashKey:{}", key, hashKey);
            return o;
        }
        return null;
    }

    @Override
    public String deleteHash(String key, String hashKey) {
        Lock lock = LockUtil.getLock("timedCache-deleteHash-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                map.remove(hashKey);
                hashOperations.put(key, map);
            }
        } finally {
            lock.unlock();
        }
        log.debug("deleteHash key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public String setHashExpireMinute(String key, String hashKey, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setHashExpireMinute-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 60 * 1000);
            } else {
                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setHashExpireMinute key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public String setHashExpireHour(String key, String hashKey, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setHashExpireHour-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 60 * 60 * 1000);
            } else {
                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setHashExpireHour key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public String setHashExpireDay(String key, String hashKey, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setHashExpireDay-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 24 * 60 * 60 * 1000);
            } else {
                ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
                map.put(hashKey, object);
                hashOperations.put(key, map, timeout * 24 * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setHashExpireDay key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public String setList(String key, Object object) {
        Lock lock = LockUtil.getLock("timedCache-setList-" + key);
        lock.lock();
        try {
            if (listOperations.containsKey(key)) {
                CopyOnWriteArrayList<Object> list = listOperations.get(key, false);
                list.add(object);
                listOperations.put(key, list);
            } else {
                CopyOnWriteArrayList<Object> list = new CopyOnWriteArrayList<>();
                list.add(object);
                listOperations.put(key, list);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setList key:{}", key);
        return "true";
    }

    @Override
    public Object getList(String key) {
        if (listOperations.containsKey(key)) {
            List<Object> list = listOperations.get(key, false);
            log.debug("getList key:{}", key);
            return list;
        }
        return null;
    }

    @Override
    public String deleteList(String key) {
        listOperations.remove(key);
        log.debug("deleteList key:{}", key);
        return "true";
    }

    @Override
    public String setListExpireMinute(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setListExpireMinute-" + key);
        lock.lock();
        try {
            if (listOperations.containsKey(key)) {
                CopyOnWriteArrayList<Object> list = listOperations.get(key, false);
                list.add(object);
                listOperations.put(key, list, timeout * 60 * 1000);
            } else {
                CopyOnWriteArrayList<Object> list = new CopyOnWriteArrayList<>();
                list.add(object);
                listOperations.put(key, list, timeout * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setListExpireMinute key:{}", key);
        return "true";
    }

    @Override
    public String setListExpireHour(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setListExpireHour-" + key);
        lock.lock();
        try {
            if (listOperations.containsKey(key)) {
                CopyOnWriteArrayList<Object> list = listOperations.get(key, false);
                list.add(object);
                listOperations.put(key, list, timeout * 60 * 60 * 1000);
            } else {
                CopyOnWriteArrayList<Object> list = new CopyOnWriteArrayList<>();
                list.add(object);
                listOperations.put(key, list, timeout * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setListExpireHour key:{}", key);
        return "true";
    }

    @Override
    public String setListExpireDay(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setListExpireDay-" + key);
        lock.lock();
        try {
            if (listOperations.containsKey(key)) {
                CopyOnWriteArrayList<Object> list = listOperations.get(key, false);
                list.add(object);
                listOperations.put(key, list, timeout * 24 * 60 * 60 * 1000);
            } else {
                CopyOnWriteArrayList<Object> list = new CopyOnWriteArrayList<>();
                list.add(object);
                listOperations.put(key, list, timeout * 24 * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setListExpireDay key:{}", key);
        return "true";
    }

    @Override
    public String deleteAll(String prefix) {
        cacheMap.clear();
        hashOperations.clear();
        listOperations.clear();
        setOperations.clear();
        log.debug("deleteAll prefix:{}", prefix);
        return "true";
    }

    @Override
    public String deleteLike(String key) {
        cacheMap.keySet().stream().filter(k -> k.contains(key)).forEach(k -> cacheMap.remove(k));
        hashOperations.keySet().stream().filter(k -> k.contains(key)).forEach(k -> hashOperations.remove(k));
        listOperations.keySet().stream().filter(k -> k.contains(key)).forEach(k -> listOperations.remove(k));
        setOperations.keySet().stream().filter(k -> k.contains(key)).forEach(k -> setOperations.remove(k));
        log.debug("deleteLike key:{}", key);
        return "true";
    }

    @Override
    public String setSet(String key, Object object) {
        Lock lock = LockUtil.getLock("timedCache-setSet-" + key);
        lock.lock();
        try {
            if (setOperations.containsKey(key)) {
                ConcurrentHashSet<Object> set = setOperations.get(key, false);
                set.add(object);
                setOperations.put(key, set);
            } else {
                ConcurrentHashSet<Object> set = new ConcurrentHashSet<>();
                set.add(object);
                setOperations.put(key, set);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setSet key:{}", key);
        return "true";
    }

    @Override
    public String setSetExpireMinute(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setSetExpireMinute-" + key);
        lock.lock();
        try {
            if (setOperations.containsKey(key)) {
                ConcurrentHashSet<Object> set = setOperations.get(key, false);
                set.add(object);
                setOperations.put(key, set, timeout * 60 * 1000);
            } else {
                ConcurrentHashSet<Object> set = new ConcurrentHashSet<>();
                set.add(object);
                setOperations.put(key, set, timeout * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setSetExpireMinute key:{}", key);
        return "true";
    }

    @Override
    public String setSetExpireHour(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setSetExpireHour-" + key);
        lock.lock();
        try {
            if (setOperations.containsKey(key)) {
                ConcurrentHashSet<Object> set = setOperations.get(key, false);
                set.add(object);
                setOperations.put(key, set, timeout * 60 * 60 * 1000);
            } else {
                ConcurrentHashSet<Object> set = new ConcurrentHashSet<>();
                set.add(object);
                setOperations.put(key, set, timeout * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setSetExpireHour key:{}", key);
        return "true";
    }

    @Override
    public String setSetExpireDay(String key, Object object, Long timeout) {
        Lock lock = LockUtil.getLock("timedCache-setSetExpireDay-" + key);
        lock.lock();
        try {
            if (setOperations.containsKey(key)) {
                ConcurrentHashSet<Object> set = setOperations.get(key, false);
                set.add(object);
                setOperations.put(key, set, timeout * 24 * 60 * 60 * 1000);
            } else {
                ConcurrentHashSet<Object> set = new ConcurrentHashSet<>();
                set.add(object);
                setOperations.put(key, set, timeout * 24 * 60 * 60 * 1000);
            }
        } finally {
            lock.unlock();
        }
        log.debug("setSetExpireDay key:{}", key);
        return "true";
    }


    @Override
    public String deleteLikeHash(String key, String hashKey) {
        Lock lock = LockUtil.getLock("timedCache-deleteLikeHash-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    if (entry.getKey().contains(hashKey)) {
                        map.remove(entry.getKey());
                    }
                }
                hashOperations.put(key, map);
            }
        } finally {
            lock.unlock();
        }
        log.debug("deleteLikeHash key:{},hashKey:{}", key, hashKey);
        return "true";
    }

    @Override
    public String deleteLeftLikeHash(String key, String hashKey) {
        Lock lock = LockUtil.getLock("timedCache-deleteLeftLikeHash-" + key);
        lock.lock();
        try {
            if (hashOperations.containsKey(key)) {
                ConcurrentHashMap<String, Object> map = hashOperations.get(key, false);
                Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    if (entry.getKey().startsWith(hashKey)) {
                        map.remove(entry.getKey());
                    }
                }
                hashOperations.put(key, map);
            }
        } finally {
            lock.unlock();
        }
        log.debug("deleteLeftLikeHash key:{},hashKey:{}", key, hashKey);
        return "true";
    }
}
