package com.campus.system.security.controller;

import com.campus.auth.dto.IpRequest;
import com.campus.auth.service.IpBlacklistService;
import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "IP 黑名单")
@RestController
@RequestMapping("/api/system/ip-blacklist")
@RequiredArgsConstructor
public class IpBlacklistController {

    private final IpBlacklistService ipBlacklistService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:ip:list')")
    @Operation(summary = "黑名单列表")
    public Result<List<String>> list() {
        return Result.success(ipBlacklistService.list());
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:ip:add')")
    @OperLog(title = "IP 黑名单", businessType = BusinessType.INSERT)
    @Operation(summary = "加入黑名单")
    public Result<Void> add(@Valid @RequestBody IpRequest request) {
        ipBlacklistService.add(request.getIp());
        return Result.success();
    }

    @DeleteMapping("/{ip}")
    @PreAuthorize("@ss.hasPermi('system:ip:remove')")
    @OperLog(title = "IP 黑名单", businessType = BusinessType.DELETE)
    @Operation(summary = "移出黑名单")
    public Result<Void> remove(@PathVariable String ip) {
        ipBlacklistService.remove(ip);
        return Result.success();
    }
}
