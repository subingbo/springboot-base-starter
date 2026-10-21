package com.campus.system.log.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.dto.IdListRequest;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.log.dto.OperLogQuery;
import com.campus.system.log.entity.OperationLog;
import com.campus.system.log.service.OperLogService;
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

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/system/operation-logs")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogService operLogService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:operlog:list')")
    @Operation(summary = "操作日志分页")
    public Result<PageResult<OperationLog>> page(@Valid OperLogQuery query) {
        return Result.success(operLogService.page(query));
    }

    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermi('system:operlog:export')")
    @OperLog(title = "操作日志", businessType = BusinessType.EXPORT, saveResult = false)
    @Operation(summary = "导出操作日志")
    public void export(@Valid OperLogQuery query, HttpServletResponse response) {
        operLogService.export(response, query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:operlog:query')")
    @Operation(summary = "操作日志详情")
    public Result<OperationLog> detail(@PathVariable Long id) {
        return Result.success(operLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:operlog:remove')")
    @OperLog(title = "操作日志", businessType = BusinessType.DELETE)
    @Operation(summary = "删除操作日志")
    public Result<Void> delete(@PathVariable Long id) {
        operLogService.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("@ss.hasPermi('system:operlog:remove')")
    @OperLog(title = "操作日志", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除操作日志")
    public Result<Void> deleteBatch(@Valid @RequestBody IdListRequest request) {
        operLogService.deleteBatch(request.getIds());
        return Result.success();
    }

    @DeleteMapping("/clean")
    @PreAuthorize("@ss.hasPermi('system:operlog:remove')")
    @OperLog(title = "操作日志", businessType = BusinessType.CLEAN)
    @Operation(summary = "按时间清理操作日志")
    public Result<Void> clean(@RequestParam
                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                              LocalDateTime before) {
        operLogService.clean(before);
        return Result.success();
    }
}
