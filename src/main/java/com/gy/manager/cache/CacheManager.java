package com.gy.manager.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 本地缓存管理器
 * 1、通过判断是否是热 Key ，如果是热 key 则存入本地缓存 Caffeine 中。
 *
 * 2、定时移除本地过期热 key。
 *
 * 3、通过每次的点赞等操作来增加热 key 计数。
 */
@Component
@Slf4j
public class CacheManager {

    private TopK hotKeyDetector;

    private Cache<String, Object> localCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public TopK getHotKeyDetector() {
        hotKeyDetector = new HeavyKeeper(
                // 监控 Top 100 Key
                100,
                // 哈希表宽度
                100000,
                // 哈希表深度
                5,
                // 衰减系数
                0.92,
                // 最小出现 10 次才记录
                10
        );
        return hotKeyDetector;
    }

    @Bean
    public Cache<String, Object> localCache() {
        return localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    // 辅助方法：构造复合 key
    private String buildCacheKey(String hashKey, String key) {
        return hashKey + ":" + key;
    }

    /**
     * 获取数据
     * @param hashKey
     * @param key 博客id
     * @return 点赞id
     */
    public Object get(String hashKey, String key) {
        // 构造唯一的 composite key
        String compositeKey = buildCacheKey(hashKey, key);

        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(compositeKey);
        if (value != null) {
            log.info("本地缓存获取到数据 {} = {}", compositeKey, value);
            // 记录访问次数（每次访问计数 +1）
            hotKeyDetector.add(key, 1);
            return value;
        }

        // 2. 本地缓存未命中，查询 Redis
        Object redisValue = redisTemplate.opsForHash().get(hashKey, key);
        if (redisValue == null) {
            return null;
        }

        // 3. 记录访问（计数 +1）
        AddResult addResult = hotKeyDetector.add(key, 1);

        // 4. 如果是热 Key 且不在本地缓存，则缓存数据
        if (addResult.isHotKey()) {
            localCache.put(compositeKey, redisValue);
        }

        return redisValue;
    }

    /**
     * 如果本地缓存中存在，则更新本地缓存
     * @param hashKey
     * @param key 博客id
     * @param value 点赞id
     */
    public void putIfPresent(String hashKey, String key, Object value) {
        String compositeKey = buildCacheKey(hashKey, key);
        Object object = localCache.getIfPresent(compositeKey);
        if (object == null) {
            return;
        }
        localCache.put(compositeKey, value);
    }

    // 定时清理过期的热 Key 检测数据
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    public void cleanHotKeys() {
        hotKeyDetector.fading();
    }
}
