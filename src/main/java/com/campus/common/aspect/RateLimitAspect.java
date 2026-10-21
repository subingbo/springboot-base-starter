package com.campus.common.aspect;

import com.campus.common.annotation.RateLimit;
import com.campus.common.ratelimit.RateLimiter;
import com.campus.common.util.IpUtils;
import com.campus.config.SecurityProperties;
import com.campus.security.user.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiter rateLimiter;
    private final SecurityProperties securityProperties;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        if (!securityProperties.isRateLimitEnabled()) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = currentRequest();
        String dimension;
        if (rateLimit.dimension() == RateLimit.Dimension.USER) {
            Long userId = SecurityUtils.getUserIdOrNull();
            dimension = userId != null
                    ? "user:" + userId
                    : "ip:" + clientIp(request);
        } else {
            dimension = "ip:" + clientIp(request);
        }
        String baseKey = rateLimit.key().isBlank()
                ? joinPoint.getSignature().toShortString()
                : rateLimit.key();
        rateLimiter.check(baseKey + ":" + dimension, rateLimit.limit(),
                Duration.ofSeconds(rateLimit.windowSeconds()));
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String clientIp(HttpServletRequest request) {
        return request == null ? "unknown"
                : IpUtils.getClientIp(request, securityProperties.isTrustProxyHeaders());
    }
}
