package com.campus.system.log.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.dto.IdListRequest;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.log.dto.LoginLogQuery;
import com.campus.system.log.entity.LoginLog;
import com.campus.system.log.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/api/system/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:loginlog:list')")
    @Operation(summary = "登录日志分页")
    public Result<PageResult<LoginLog>> page(@Valid LoginLogQuery query) {
        return Result.success(loginLogService.page(query));
    }

    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermi('system:loginlog:export')")
    @OperLog(title = "登录日志", businessType = BusinessType.EXPORT, saveResult = false)
    @Operation(summary = "导出登录日志")
    public void export(@Valid LoginLogQuery query, HttpServletResponse response) {
        loginLogService.export(response, query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:loginlog:query')")
    @Operation(summary = "登录日志详情")
    public Result<LoginLog> detail(@PathVariable Long id) {
        return Result.success(loginLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:loginlog:remove')")
    @OperLog(title = "登录日志", businessType = BusinessType.DELETE)
    @Operation(summary = "删除登录日志")
    public Result<Void> delete(@PathVariable Long id) {
        loginLogService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("@ss.hasPermi('system:loginlog:remove')")
    @OperLog(title = "登录日志", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除登录日志")
    public Result<Void> deleteBatch(@Valid @RequestBody IdListRequest request) {
        loginLogService.deleteBatch(request.getIds());
        return Result.success();
    }

    @DeleteMapping("/clean")
    @PreAuthorize("@ss.hasPermi('system:loginlog:remove')")
    @OperLog(title = "登录日志", businessType = BusinessType.CLEAN)
    @Operation(summary = "按时间清理登录日志")
    public Result<Void> clean(@RequestParam
                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                              LocalDateTime before) {
        loginLogService.clean(before);
        return Result.success();
    }
}
