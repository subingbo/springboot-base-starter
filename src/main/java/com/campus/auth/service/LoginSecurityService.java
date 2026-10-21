package com.campus.auth.service;

import com.campus.common.cache.CacheKeys;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.AuthErrorCode;
import com.campus.config.AuthProperties;
import com.campus.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginSecurityService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SysConfigService sysConfigService;
    private final AuthProperties authProperties;
    private final IpBlacklistService ipBlacklistService;

    public void assertNotBlocked(String ip, String username) {
        if (ipBlacklistService.contains(ip)) {
            throw new BusinessException(AuthErrorCode.IP_BLOCKED);
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(CacheKeys.loginLock(username)))) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailure(String username) {
        int maxFail = sysConfigService.getInt("sys.account.maxLoginFail", authProperties.getMaxLoginFail());
        int lockMinutes = sysConfigService.getInt("sys.account.lockMinutes", authProperties.getLockMinutes());
        String failKey = CacheKeys.loginFail(username);
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(failKey, Duration.ofMinutes(lockMinutes));
        }
        if (count != null && count >= maxFail) {
            stringRedisTemplate.opsForValue().set(
                    CacheKeys.loginLock(username), "1", Duration.ofMinutes(lockMinutes));
            stringRedisTemplate.delete(failKey);
        }
    }

    public void clearFailure(String username) {
        stringRedisTemplate.delete(CacheKeys.loginFail(username));
        stringRedisTemplate.delete(CacheKeys.loginLock(username));
    }
}
