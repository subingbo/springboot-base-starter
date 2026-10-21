package com.campus.system.user.service.impl;

import com.campus.auth.service.PasswordPolicy;
import com.campus.auth.service.TokenSessionService;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.AuthErrorCode;
import com.campus.common.result.ResultCode;
import com.campus.common.result.SystemErrorCode;
import com.campus.security.user.LoginUser;
import com.campus.security.user.SecurityUtils;
import com.campus.system.dept.entity.Dept;
import com.campus.system.dept.mapper.DeptMapper;
import com.campus.system.menu.mapper.MenuMapper;
import com.campus.system.role.mapper.RoleMapper;
import com.campus.system.user.dto.UpdateProfileRequest;
import com.campus.system.user.dto.UserProfileResponse;
import com.campus.system.user.entity.User;
import com.campus.system.user.mapper.UserMapper;
import com.campus.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final TokenSessionService tokenSessionService;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public LoginUser loadLoginUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }
        List<String> roles = roleMapper.selectCodesByUserId(id);
        List<String> permissions = menuMapper.selectPermsByUserId(id);
        return LoginUser.from(user, roles, permissions);
    }

    @Override
    @Transactional
    public User createUser(String username, String password, String nickname, String email, String roleCode) {
        if (userMapper.countByUsername(username) > 0) {
            throw new BusinessException(AuthErrorCode.USERNAME_EXISTS);
        }
        Long roleId = roleMapper.selectIdByCode(roleCode);
        if (roleId == null) {
            throw new BusinessException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        passwordPolicy.validate(password, username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        user.setEmail(email);
        user.setSex(0);
        user.setStatus(1);
        user.setTokenVersion(0);
        userMapper.insert(user);
        userMapper.insertUserRole(user.getId(), roleId);
        user.setPassword(null);
        return user;
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = requireUser(userId);
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setSex(user.getSex());
        response.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            Dept dept = deptMapper.selectById(user.getDeptId());
            response.setDeptName(dept == null ? null : dept.getName());
        }
        response.setRoles(roleMapper.selectCodesByUserId(userId));
        response.setPermissions(menuMapper.selectPermsByUserId(userId));
        return response;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        int rows = userMapper.updateById(user);
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        tokenSessionService.evictLoginUser(userId);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(AuthErrorCode.OLD_PASSWORD_MISMATCH);
        }
        passwordPolicy.validate(newPassword, user.getUsername());
        int rows = userMapper.updatePassword(userId, passwordEncoder.encode(newPassword),
                SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.OPTIMISTIC_LOCK);
        }
        tokenSessionService.revokeUserSessions(userId);
        tokenSessionService.evictLoginUser(userId);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        int rows = userMapper.updateAvatar(userId, avatarUrl, SecurityUtils.getUserIdOrNull());
        if (rows != 1) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        tokenSessionService.evictLoginUser(userId);
    }

    @Override
    public void updateLoginInfo(Long userId, String ip, LocalDateTime loginTime) {
        userMapper.updateLoginInfo(userId, ip, loginTime);
    }

    @Override
    @Transactional
    public void forceLogout(Long userId, boolean incrementVersion) {
        if (incrementVersion) {
            userMapper.incrementTokenVersion(userId);
        }
        tokenSessionService.revokeUserSessions(userId);
        tokenSessionService.evictLoginUser(userId);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(SystemErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
