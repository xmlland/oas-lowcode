package com.jeestudio.bpm.modules.oa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 业务编号生成服务
 * 编号格式：前缀 + yyyyMMdd + 3位序列号
 * 使用 Redis 存储当天最大序号，本地锁保证并发安全
 */
@Service
public class SerialNoService {

    private static final Logger logger = LoggerFactory.getLogger(SerialNoService.class);

    private static final String REDIS_KEY_PREFIX = "serial_no:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成业务编号
     *
     * @param prefix 编号前缀，如 "BK"、"CG" 等
     * @return 生成的编号，如 BK20260301001
     */
    public String generateSerialNo(String prefix) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = REDIS_KEY_PREFIX + prefix + ":" + today;

        // 按 redisKey 粒度加锁，不同前缀互不阻塞
        Lock lock = lockMap.computeIfAbsent(redisKey, k -> new ReentrantLock());
        lock.lock();
        try {
            Long seq = redisTemplate.opsForValue().increment(redisKey);
            if (seq != null && seq == 1L) {
                // 新 key，设置过期时间为2天（跨天后自动清理）
                redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
            }
            return prefix + today + String.format("%03d", seq);
        } catch (Exception e) {
            logger.error("生成编号失败, prefix={}", prefix, e);
            throw new RuntimeException("生成编号失败");
        } finally {
            lock.unlock();
        }
    }
}
