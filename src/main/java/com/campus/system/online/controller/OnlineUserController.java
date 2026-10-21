package com.campus.system.online.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.online.dto.OnlineUserQuery;
import com.campus.system.online.dto.OnlineUserResponse;
import com.campus.system.online.service.OnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "在线用户")
@RestController
@RequestMapping("/api/system/online-users")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:online:list')")
    @Operation(summary = "在线用户分页")
    public Result<PageResult<OnlineUserResponse>> page(@Valid OnlineUserQuery query) {
        return Result.success(onlineUserService.page(query));
    }

    @DeleteMapping("/{tokenId}")
    @PreAuthorize("@ss.hasPermi('system:online:forceLogout')")
    @OperLog(title = "在线用户", businessType = BusinessType.FORCE_LOGOUT)
    @Operation(summary = "按会话强制下线")
    public Result<Void> forceLogout(@PathVariable String tokenId) {
        onlineUserService.forceLogout(tokenId);
        return Result.success();
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("@ss.hasPermi('system:online:forceLogout')")
    @OperLog(title = "在线用户", businessType = BusinessType.FORCE_LOGOUT)
    @Operation(summary = "按用户强制下线")
    public Result<Void> forceLogoutUser(@PathVariable Long userId) {
        onlineUserService.forceLogoutUser(userId);
        return Result.success();
    }
}
