package com.campus.system.dept.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.result.Result;
import com.campus.system.dept.dto.DeptRequest;
import com.campus.system.dept.dto.DeptResponse;
import com.campus.system.dept.entity.Dept;
import com.campus.system.dept.service.DeptService;
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

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @Operation(summary = "部门树")
    public Result<List<DeptResponse>> tree() {
        return Result.success(deptService.tree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:dept:query')")
    @Operation(summary = "部门详情")
    public Result<Dept> detail(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:dept:add')")
    @OperLog(title = "部门管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增部门")
    public Result<Long> create(@Valid @RequestBody DeptRequest request) {
        return Result.success(deptService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:dept:edit')")
    @OperLog(title = "部门管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改部门")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DeptRequest request) {
        deptService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:dept:remove')")
    @OperLog(title = "部门管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除部门")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }
}
