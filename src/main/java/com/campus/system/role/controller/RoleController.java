package com.campus.system.role.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.dto.IdListRequest;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.role.dto.RoleQuery;
import com.campus.system.role.dto.RoleRequest;
import com.campus.system.role.dto.RoleResponse;
import com.campus.system.role.service.RoleService;
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

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @Operation(summary = "角色分页")
    public Result<PageResult<RoleResponse>> page(RoleQuery query) {
        return Result.success(roleService.page(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @Operation(summary = "角色详情")
    public Result<RoleResponse> detail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @OperLog(title = "角色管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增角色")
    public Result<Long> create(@Valid @RequestBody RoleRequest request) {
        return Result.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @OperLog(title = "角色管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改角色")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @OperLog(title = "角色管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("@ss.hasPermi('system:role:assignMenu')")
    @OperLog(title = "角色管理", businessType = BusinessType.GRANT)
    @Operation(summary = "分配菜单")
    public Result<Void> assignMenus(@PathVariable Long id, @Valid @RequestBody IdListRequest request) {
        roleService.assignMenus(id, request.getIds());
        return Result.success();
    }

    @PutMapping("/{id}/depts")
    @PreAuthorize("@ss.hasPermi('system:role:assignDept')")
    @OperLog(title = "角色管理", businessType = BusinessType.GRANT)
    @Operation(summary = "分配自定义数据部门")
    public Result<Void> assignDepts(@PathVariable Long id, @Valid @RequestBody IdListRequest request) {
        roleService.assignDepts(id, request.getIds());
        return Result.success();
    }
}
