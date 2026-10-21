package com.campus.security.user;

import com.campus.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 方法级权限表达式入口，ADMIN 直接放行，避免逐个按钮授权导致管理员被误锁。
 */
@Component("ss")
@RequiredArgsConstructor
public class PermissionService {

    private final SecurityProperties securityProperties;

    public boolean hasPermi(String permission) {
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        if (loginUser == null) {
            return false;
        }
        if (loginUser.getRoles().contains(securityProperties.getAdminRoleCode())) {
            return true;
        }
        return loginUser.getPermissions().contains(permission);
    }

    public boolean hasRole(String role) {
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        return loginUser != null && loginUser.getRoles().contains(role);
    }

    public boolean isAdmin() {
        return SecurityUtils.isAdmin(securityProperties.getAdminRoleCode());
    }
}
