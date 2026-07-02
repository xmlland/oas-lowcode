package com.jeestudio.bpm.utils;

import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 锁工具
 */
public class LockUtil {
    static final Hashtable<String, Lock> lockMap = new Hashtable<>();

    /**
     * 获取锁
     *
     * @param lockKey 锁的key
     * @return
     */
    public static Lock getLock(String lockKey) {
        synchronized (lockMap) {
            if (!lockMap.containsKey(lockKey)) {
                lockMap.put(lockKey, new ReentrantLock());
            }
        }
        return lockMap.get(lockKey);
    }
}
