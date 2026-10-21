package com.campus.system.user.controller;

import com.campus.common.result.Result;
import com.campus.file.dto.FileResponse;
import com.campus.file.service.FileStorageService;
import com.campus.security.user.SecurityUtils;
import com.campus.system.user.dto.ChangePasswordRequest;
import com.campus.system.user.dto.UpdateProfileRequest;
import com.campus.system.user.dto.UserProfileResponse;
import com.campus.system.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "当前登录用户信息")
    public Result<UserProfileResponse> profile() {
        return Result.success(userService.getProfile(SecurityUtils.getUserId()));
    }

    @PutMapping
    @Operation(summary = "修改个人资料")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(SecurityUtils.getUserId(), request);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码，成功后所有设备下线")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.updatePassword(SecurityUtils.getUserId(),
                request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像")
    public Result<FileResponse> avatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getUserId();
        FileResponse response = fileStorageService.store(file, "AVATAR", userId);
        userService.updateAvatar(userId, response.getUrl());
        return Result.success(response);
    }
}
