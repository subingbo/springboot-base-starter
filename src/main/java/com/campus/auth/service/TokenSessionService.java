package com.campus.auth.service;

import com.campus.auth.dto.OnlineSession;
import com.campus.auth.dto.RefreshResult;
import com.campus.auth.dto.RefreshSession;
import com.campus.auth.dto.TokenResponse;
import com.campus.common.cache.CacheKeys;
import com.campus.security.jwt.JwtProperties;
import com.campus.security.user.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;

    public void saveRefreshSession(RefreshSession session) {
        Duration ttl = refreshTtl();
        putJson(CacheKeys.refreshToken(session.getTokenId()), session, ttl);
        String familyKey = CacheKeys.refreshFamily(session.getFamilyId());
        stringRedisTemplate.opsForSet().add(familyKey, session.getTokenId());
        stringRedisTemplate.expire(familyKey, ttl);
        String userFamilyKey = CacheKeys.userFamilies(session.getUserId());
        stringRedisTemplate.opsForSet().add(userFamilyKey, session.getFamilyId());
        stringRedisTemplate.expire(userFamilyKey, ttl);
    }

    public RefreshSession consumeRefreshSession(String tokenId) {
        String json = stringRedisTemplate.opsForValue().getAndDelete(CacheKeys.refreshToken(tokenId));
        return readJson(json, RefreshSession.class);
    }

    public void deleteRefreshSession(String tokenId) {
        stringRedisTemplate.delete(CacheKeys.refreshToken(tokenId));
    }

    public boolean isFamilyMember(String familyId, String tokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet()
                .isMember(CacheKeys.refreshFamily(familyId), tokenId));
    }

    public void removeFamilyMember(String familyId, String tokenId) {
        String key = CacheKeys.refreshFamily(familyId);
        stringRedisTemplate.opsForSet().remove(key, tokenId);
        Long size = stringRedisTemplate.opsForSet().size(key);
        if (size != null && size == 0) {
            stringRedisTemplate.delete(key);
        }
    }

    public boolean isFamilyEmpty(String familyId) {
        return !Boolean.TRUE.equals(stringRedisTemplate.hasKey(CacheKeys.refreshFamily(familyId)));
    }

    public void removeUserFamily(Long userId, String familyId) {
        String key = CacheKeys.userFamilies(userId);
        stringRedisTemplate.opsForSet().remove(key, familyId);
        Long size = stringRedisTemplate.opsForSet().size(key);
        if (size != null && size == 0) {
            stringRedisTemplate.delete(key);
        }
    }

    public void revokeFamily(String familyId) {
        if (familyId == null) {
            return;
        }
        Set<String> tokenIds = stringRedisTemplate.opsForSet().members(CacheKeys.refreshFamily(familyId));
        if (tokenIds != null) {
            for (String tokenId : tokenIds) {
                deleteRefreshSession(tokenId);
                removeOnlineSession(tokenId);
            }
        }
        stringRedisTemplate.delete(CacheKeys.refreshFamily(familyId));
        stringRedisTemplate.opsForValue().set(CacheKeys.blacklistFamily(familyId), "1", accessTtl());
    }

    public void revokeUserSessions(Long userId) {
        Set<String> families = stringRedisTemplate.opsForSet().members(CacheKeys.userFamilies(userId));
        if (families != null) {
            for (String familyId : families) {
                revokeFamily(familyId);
            }
        }
        stringRedisTemplate.delete(CacheKeys.userFamilies(userId));
        removeOnlineSessionsByUserId(userId);
    }

    public boolean isTokenBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(CacheKeys.blacklist(tokenId)));
    }

    public boolean isFamilyBlacklisted(String familyId) {
        return familyId != null
                && Boolean.TRUE.equals(stringRedisTemplate.hasKey(CacheKeys.blacklistFamily(familyId)));
    }

    public void blacklistToken(String tokenId, long ttlSeconds) {
        if (tokenId == null || ttlSeconds <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue()
                .set(CacheKeys.blacklist(tokenId), "1", Duration.ofSeconds(ttlSeconds));
    }

    public void cacheLoginUser(LoginUser loginUser, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                CacheKeys.loginUser(loginUser.getId()),
                loginUser,
                Duration.ofSeconds(Math.max(60, ttlSeconds))
        );
    }

    public LoginUser getLoginUser(Long userId) {
        Object value = redisTemplate.opsForValue().get(CacheKeys.loginUser(userId));
        return value instanceof LoginUser loginUser ? loginUser : null;
    }

    public void evictLoginUser(Long userId) {
        redisTemplate.delete(CacheKeys.loginUser(userId));
    }

    public void evictLoginUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<String> keys = userIds.stream().map(CacheKeys::loginUser).toList();
        redisTemplate.delete(keys);
    }

    public void saveOnlineSession(OnlineSession session) {
        putJson(CacheKeys.online(session.getTokenId()), session, refreshTtl());
        long score = session.getLoginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        stringRedisTemplate.opsForZSet().add(CacheKeys.onlineIndex(), session.getTokenId(), score);
        stringRedisTemplate.expire(CacheKeys.onlineIndex(), refreshTtl());
    }

    public OnlineSession getOnlineSession(String tokenId) {
        String json = stringRedisTemplate.opsForValue().get(CacheKeys.online(tokenId));
        return readJson(json, OnlineSession.class);
    }

    public void removeOnlineSession(String tokenId) {
        stringRedisTemplate.delete(CacheKeys.online(tokenId));
        stringRedisTemplate.opsForZSet().remove(CacheKeys.onlineIndex(), tokenId);
    }

    public void removeOnlineSessionsByUserId(Long userId) {
        for (OnlineSession session : listOnlineSessions()) {
            if (userId.equals(session.getUserId())) {
                removeOnlineSession(session.getTokenId());
            }
        }
    }

    public List<OnlineSession> listOnlineSessions() {
        Set<String> tokenIds = stringRedisTemplate.opsForZSet()
                .reverseRange(CacheKeys.onlineIndex(), 0, -1);
        if (tokenIds == null || tokenIds.isEmpty()) {
            return List.of();
        }
        List<OnlineSession> sessions = new ArrayList<>();
        for (String tokenId : tokenIds) {
            String json = stringRedisTemplate.opsForValue().get(CacheKeys.online(tokenId));
            if (json == null) {
                stringRedisTemplate.opsForZSet().remove(CacheKeys.onlineIndex(), tokenId);
                continue;
            }
            OnlineSession session = readJson(json, OnlineSession.class);
            if (session != null) {
                sessions.add(session);
            }
        }
        sessions.sort(Comparator.comparing(OnlineSession::getLoginTime).reversed());
        return sessions;
    }

    public void saveRefreshResult(String oldTokenId, TokenResponse response, String ip,
                                  String userAgent, int graceSeconds) {
        if (graceSeconds <= 0) {
            return;
        }
        RefreshResult result = new RefreshResult(response, ip, userAgent, LocalDateTime.now());
        putJson(CacheKeys.refreshResult(oldTokenId), result, Duration.ofSeconds(graceSeconds));
    }

    public RefreshResult getRefreshResult(String oldTokenId) {
        String json = stringRedisTemplate.opsForValue().get(CacheKeys.refreshResult(oldTokenId));
        return readJson(json, RefreshResult.class);
    }

    public void removeRefreshResult(String oldTokenId) {
        stringRedisTemplate.delete(CacheKeys.refreshResult(oldTokenId));
    }

    private Duration refreshTtl() {
        return Duration.ofSeconds(jwtProperties.getRefreshTokenExpiration());
    }

    private Duration accessTtl() {
        return Duration.ofSeconds(jwtProperties.getAccessTokenExpiration());
    }

    private <T> T readJson(String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception ex) {
            log.warn("failed to deserialize redis session type={}", type.getSimpleName(), ex);
            return null;
        }
    }

    private void putJson(String key, Object value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to serialize redis session", ex);
        }
    }
}
