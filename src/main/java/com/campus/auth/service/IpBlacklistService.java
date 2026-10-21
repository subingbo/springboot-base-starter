package com.campus.auth.service;

import com.campus.common.cache.CacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IpBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean contains(String ip) {
        return ip != null && Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(CacheKeys.ipBlacklist(), ip));
    }

    public void add(String ip) {
        stringRedisTemplate.opsForSet().add(CacheKeys.ipBlacklist(), ip);
    }

    public void remove(String ip) {
        stringRedisTemplate.opsForSet().remove(CacheKeys.ipBlacklist(), ip);
    }

    public List<String> list() {
        Set<String> members = stringRedisTemplate.opsForSet().members(CacheKeys.ipBlacklist());
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(members);
        result.sort(String::compareTo);
        return result;
    }
}
