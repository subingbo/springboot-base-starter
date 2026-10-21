package com.campus.system.config.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.config.dto.ConfigQuery;
import com.campus.system.config.dto.ConfigRequest;
import com.campus.system.config.entity.SysConfig;
import com.campus.system.config.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "参数配置")
@RestController
@RequestMapping("/api/system/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @Operation(summary = "参数分页")
    public Result<PageResult<SysConfig>> page(@Valid ConfigQuery query) {
        return Result.success(configService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @Operation(summary = "参数详情")
    public Result<SysConfig> detail(@PathVariable Long id) {
        return Result.success(configService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @OperLog(title = "参数配置", businessType = BusinessType.INSERT)
    @Operation(summary = "新增参数")
    public Result<Long> create(@Valid @RequestBody ConfigRequest request) {
        return Result.success(configService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @OperLog(title = "参数配置", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改参数")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ConfigRequest request) {
        configService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @OperLog(title = "参数配置", businessType = BusinessType.DELETE)
    @Operation(summary = "删除参数")
    public Result<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return Result.success();
    }
}
