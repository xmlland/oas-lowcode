package com.jeestudio.bpm.cache.cacheUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @Description: 缓存接口
 */
public interface CacheI {
    String getCachePrefix();

    Boolean getLoginModeSingle();

    Boolean exist(String key);

    Long expire(String key);

    int size();

    Set<String> keys();

    Collection<Object> values();

    Integer sizeLike(String key);

    Set<String> keysLike(String key);

    Collection<Object> valuesLike(String key);

    String setObject(String key, Object object);

    Object getObject(String key);

    String deleteObject(String key);

    String setObjectExpireMinute(String key, Object object, Long timeout);

    String setObjectExpireHour(String key, Object object, Long timeout);

    String setObjectExpireDay(String key, Object object, Long timeout);

    Set<String> getHashKeys(String key);

    String setHash(String key, String hashKey, Object object);

    Object getHash(String key, String hashKey);

    String deleteHash(String key, String hashKey);

    String setHashExpireMinute(String key, String hashKey, Object object, Long timeout);

    String setHashExpireHour(String key, String hashKey, Object object, Long timeout);

    String setHashExpireDay(String key, String hashKey, Object object, Long timeout);

    String setList(String key, Object object);

    Object getList(String key);

    String deleteList(String key);

    String setListExpireMinute(String key, Object object, Long timeout);

    String setListExpireHour(String key, Object object, Long timeout);

    String setListExpireDay(String key, Object object, Long timeout);

    String deleteAll(String prefix);

    String deleteLike(String key);

    String setSet(String key, Object object);

    String setSetExpireMinute(String key, Object object, Long timeout);

    String setSetExpireHour(String key, Object object, Long timeout);

    String setSetExpireDay(String key, Object object, Long timeout);

    default String setZset(String key, Object object, double num){
        throw new UnsupportedOperationException();
    }

    default String setZsetExpireMinute(String key, Object object, double num, Long timeout){
        throw new UnsupportedOperationException();
    }

    default String setZsetExpireHour(String key, Object object, double num, Long timeout){
        throw new UnsupportedOperationException();
    }

    default String setZsetExipreDay(String key, Object object, double num, Long timeout){
        throw new UnsupportedOperationException();
    }

    String deleteLikeHash(String key, String hashKey);

    String deleteLeftLikeHash(String key, String hashKey);
}
