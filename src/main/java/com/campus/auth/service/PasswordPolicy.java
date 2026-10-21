package com.campus.auth.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.AuthErrorCode;
import com.campus.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PasswordPolicy {

    private static final Set<String> WEAK_PASSWORDS = Set.of(
            "12345678", "123456789", "password", "admin123",
            "qwerty123", "abc12345", "11111111"
    );

    private final AuthProperties authProperties;

    public void validate(String password, String username) {
        if (password == null) {
            throw violation("密码不能为空");
        }
        int min = authProperties.getPasswordMinLength();
        int max = authProperties.getPasswordMaxLength();
        if (password.length() < min || password.length() > max) {
            throw violation("密码长度必须为 " + min + "-" + max + " 位");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw violation("密码不能包含空白字符");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw violation("密码必须同时包含字母和数字");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            throw violation("密码不能与用户名相同");
        }
        if (WEAK_PASSWORDS.contains(password.toLowerCase(Locale.ROOT))) {
            throw violation("密码过于简单，请更换更复杂的密码");
        }
    }

    private BusinessException violation(String message) {
        return new BusinessException(AuthErrorCode.PASSWORD_POLICY_VIOLATION, message);
    }
}
