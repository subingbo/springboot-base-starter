package com.campus.common.aspect;

import com.campus.common.annotation.RepeatSubmit;
import com.campus.common.cache.CacheKeys;
import com.campus.common.cache.CacheService;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.CommonErrorCode;
import com.campus.common.util.IpUtils;
import com.campus.common.web.CachedBodyFilter;
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
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/**
 * 同一用户/IP 在短时间内的重复请求拦截。JSON Body 不参与摘要，
 * 需要业务级幂等时应在请求头提供幂等键并由业务表保证。
 */
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final CacheService cacheService;
    private final SecurityProperties securityProperties;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        if (!securityProperties.isRepeatSubmitEnabled()) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = currentRequest();
        String bodyHash = bodyHash(request);
        if (bodyHash == null) {
            // JSON 请求体尚未缓存时无法区分不同请求，跳过检查，避免误伤。
            return joinPoint.proceed();
        }
        String subject = resolveSubject(request);
        String uri = request == null ? joinPoint.getSignature().toShortString() : request.getRequestURI();
        String method = request == null ? "" : request.getMethod();
        String key = subject + ":" + method + ":" + uri + ":" + paramHash(request) + ":" + bodyHash;
        boolean acquired = cacheService.setIfAbsent(CacheKeys.repeat(key), "1",
                Duration.ofSeconds(Math.max(1, repeatSubmit.intervalSeconds())));
        if (!acquired) {
            throw new BusinessException(CommonErrorCode.REPEAT_SUBMIT, repeatSubmit.message());
        }
        return joinPoint.proceed();
    }

    private String resolveSubject(HttpServletRequest request) {
        Long userId = SecurityUtils.getUserIdOrNull();
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + (request == null ? "unknown"
                : IpUtils.getClientIp(request, securityProperties.isTrustProxyHeaders()));
    }

    private String paramHash(HttpServletRequest request) {
        if (request == null) {
            return "none";
        }
        Map<String, String[]> parameterMap = new TreeMap<>(request.getParameterMap());
        StringBuilder builder = new StringBuilder();
        parameterMap.forEach((key, values) -> builder.append(key).append('=')
                .append(String.join(",", values)).append('&'));
        return sha256(builder.toString());
    }

    private String bodyHash(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(CachedBodyFilter.WRAPPER_ATTRIBUTE);
        ContentCachingRequestWrapper wrapper = null;
        if (attribute instanceof ContentCachingRequestWrapper cached) {
            wrapper = cached;
        } else if (request instanceof ContentCachingRequestWrapper cached) {
            wrapper = cached;
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            if (wrapper == null) {
                return null;
            }
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }
            return sha256(new String(content, StandardCharsets.UTF_8));
        }
        return "none";
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
