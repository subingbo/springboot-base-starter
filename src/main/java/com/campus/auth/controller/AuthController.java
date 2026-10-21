package com.campus.auth.controller;

import com.campus.auth.dto.CaptchaResponse;
import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.RefreshRequest;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.dto.TokenResponse;
import com.campus.auth.service.AuthService;
import com.campus.auth.service.CaptchaService;
import com.campus.common.annotation.Anonymous;
import com.campus.common.annotation.RateLimit;
import com.campus.common.annotation.RepeatSubmit;
import com.campus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @Anonymous
    @GetMapping("/captcha")
    @RateLimit(key = "auth:captcha", limit = 20, windowSeconds = 60)
    @Operation(summary = "获取图形验证码")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.generate());
    }

    @Anonymous
    @PostMapping("/register")
    @RepeatSubmit(intervalSeconds = 5)
    @RateLimit(key = "auth:register", limit = 5, windowSeconds = 60)
    @Operation(summary = "注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Anonymous
    @PostMapping("/login")
    @RateLimit(key = "auth:login", limit = 10, windowSeconds = 60)
    @Operation(summary = "登录")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        return Result.success(authService.login(request, httpRequest));
    }

    @Anonymous
    @PostMapping("/refresh")
    @RateLimit(key = "auth:refresh", limit = 30, windowSeconds = 60)
    @Operation(summary = "刷新访问令牌")
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                         HttpServletRequest httpRequest) {
        return Result.success(authService.refresh(request, httpRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出当前会话")
    public Result<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                               String authorization,
                               HttpServletRequest httpRequest) {
        authService.logout(resolveToken(authorization), httpRequest);
        return Result.success();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "退出所有设备")
    public Result<Void> logoutAll() {
        authService.logoutAll();
        return Result.success();
    }

    private String resolveToken(String authorization) {
        if (StringUtils.hasText(authorization)
                && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        return null;
    }
}
