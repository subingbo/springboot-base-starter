package com.campus.system.user.service;

import com.campus.security.user.LoginUser;
import com.campus.system.user.dto.UpdateProfileRequest;
import com.campus.system.user.dto.UserProfileResponse;
import com.campus.system.user.entity.User;

import java.time.LocalDateTime;

public interface UserService {

    User getById(Long id);

    User getByUsername(String username);

    LoginUser loadLoginUser(Long id);

    User createUser(String username, String password, String nickname, String email, String roleCode);

    UserProfileResponse getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateAvatar(Long userId, String avatarUrl);

    void updateLoginInfo(Long userId, String ip, LocalDateTime loginTime);

    void forceLogout(Long userId, boolean incrementVersion);
}
