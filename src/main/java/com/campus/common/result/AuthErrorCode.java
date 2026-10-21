package com.campus.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    CAPTCHA_INVALID(10001, "验证码错误或已过期", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(10002, "登录失败次数过多，请稍后重试", HttpStatus.LOCKED),
    INVALID_CREDENTIALS(10003, "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(10004, "账号已禁用", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_REUSED(10005, "刷新令牌已失效，请重新登录", HttpStatus.UNAUTHORIZED),
    PASSWORD_POLICY_VIOLATION(10006, "密码不符合安全策略", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_MISMATCH(10007, "原密码错误", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_INVALID(10008, "刷新令牌无效", HttpStatus.UNAUTHORIZED),
    IP_BLOCKED(10009, "IP 已被限制访问", HttpStatus.FORBIDDEN),
    REGISTER_DISABLED(10010, "当前未开放注册", HttpStatus.FORBIDDEN),
    USERNAME_EXISTS(10011, "用户名已存在", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
