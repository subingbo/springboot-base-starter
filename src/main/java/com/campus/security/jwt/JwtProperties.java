package com.campus.security.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "campus.jwt")
public class JwtProperties {

    @NotBlank
    @Size(min = 32, message = "JWT secret 至少需要 32 个字符")
    private String secret;

    @Min(60)
    private long accessTokenExpiration = 1800;

    @Min(300)
    private long refreshTokenExpiration = 604800;
}
