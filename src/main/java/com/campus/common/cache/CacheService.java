package com.campus.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Duration NULL_TTL = Duration.ofSeconds(60);

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof NullValue) {
            return null;
        }
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            log.warn("cache type mismatch, evict key={}", key);
            redisTemplate.delete(key);
            return null;
        }
        return type.cast(value);
    }

    public void put(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 分区 TTL 加随机抖动，降低缓存雪崩风险。
     */
    public void putWithJitter(String key, Object value, Duration ttl) {
        long seconds = Math.max(1, ttl.toSeconds());
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(2, seconds / 10));
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds + jitter));
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof NullValue) {
            return null;
        }
        if (cached != null) {
            try {
                return (T) cached;
            } catch (ClassCastException ex) {
                redisTemplate.delete(key);
            }
        }
        T value = loader.get();
        if (value == null) {
            redisTemplate.opsForValue().set(key, NullValue.INSTANCE, NULL_TTL);
        } else {
            putWithJitter(key, value, ttl);
        }
        return value;
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 使用 SCAN 分批清理，禁止 KEYS 阻塞 Redis。
     */
    public void evictByPrefix(String prefix) {
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(500).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        return Boolean.TRUE.equals(result);
    }

    public long increment(String key, Duration ttl) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, ttl);
        }
        return count == null ? 0L : count;
    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void putString(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
}
