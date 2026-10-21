package com.campus.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class IpUtils {

    private static final String UNKNOWN = "unknown";

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        return getClientIp(request, false);
    }

    /**
     * 默认不信任 X-Forwarded-For，避免客户端伪造请求头绕过限流；
     * 仅当应用确实部署在可信代理之后时，通过配置显式开启。
     */
    public static String getClientIp(HttpServletRequest request, boolean trustProxyHeaders) {
        String ip = null;
        if (trustProxyHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(forwarded)) {
                ip = forwarded.split(",")[0].trim();
            }
            if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return sanitize(ip, request.getRemoteAddr());
    }

    private static String sanitize(String ip, String fallback) {
        if (!StringUtils.hasText(ip) || ip.length() > 64) {
            return fallback;
        }
        if (!ip.matches("[0-9a-fA-F:.%\\[\\]]+")) {
            return fallback;
        }
        return ip;
    }
}
