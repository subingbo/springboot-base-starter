package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.auth")
public class AuthProperties {

    private boolean captchaEnabled = true;

    private int maxLoginFail = 5;

    private int lockMinutes = 15;

    private boolean registerEnabled = true;

    private int passwordMinLength = 8;

    private int passwordMaxLength = 32;

    /**
     * 刷新令牌并发重试宽限期，0 表示严格重放检测。
     */
    private int refreshGraceSeconds = 10;
}
