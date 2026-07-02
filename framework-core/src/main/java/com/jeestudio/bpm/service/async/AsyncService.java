package com.jeestudio.bpm.service.async;

import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Description: 异步任务服务
 */
@Component
public class AsyncService {

    private static CacheUtil cacheUtil = new CacheUtil();

    @Async
    public void asyncSaveHashCache(String key, String hashKey, String value) {
        cacheUtil.setHashCache(key, hashKey, value);
    }

    @Async
    public void deleteListHash(String key, String hashKey) {
        cacheUtil.deleteLikeHashCache(key, hashKey);
    }

    @Async
    public void deleteListHashStartWithHashKey(String key, String hashKey) {
        cacheUtil.deleteLeftLikeHashCache(key, hashKey);
    }
}
