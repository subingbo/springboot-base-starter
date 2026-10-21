package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.security")
public class SecurityProperties {

    /**
     * 是否信任 X-Forwarded-For / X-Real-IP，仅在可信代理之后开启。
     */
    private boolean trustProxyHeaders = false;

    private String adminRoleCode = "ADMIN";

    private boolean rateLimitEnabled = true;

    private boolean repeatSubmitEnabled = true;
}
