package com.campus.system.user.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.dto.IdListRequest;
import com.campus.common.dto.StatusRequest;
import com.campus.common.enums.BusinessType;
import com.campus.common.excel.ImportResult;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.user.dto.ResetPasswordRequest;
import com.campus.system.user.dto.UserCreateRequest;
import com.campus.system.user.dto.UserQuery;
import com.campus.system.user.dto.UserResponse;
import com.campus.system.user.dto.UserUpdateRequest;
import com.campus.system.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @Operation(summary = "用户分页")
    public Result<PageResult<UserResponse>> page(@Valid UserQuery query) {
        return Result.success(sysUserService.page(query));
    }

    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @OperLog(title = "用户管理", businessType = BusinessType.EXPORT, saveResult = false)
    @Operation(summary = "导出用户（游标分批）")
    public void export(@Valid UserQuery query, HttpServletResponse response) {
        sysUserService.exportUsers(response, query);
    }

    @GetMapping("/import-template")
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @Operation(summary = "下载用户导入模板")
    public void importTemplate(HttpServletResponse response) {
        sysUserService.writeImportTemplate(response);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @OperLog(title = "用户管理", businessType = BusinessType.IMPORT)
    @Operation(summary = "导入用户")
    public Result<ImportResult> importUsers(@RequestParam("file") MultipartFile file) {
        return Result.success(sysUserService.importUsers(file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @Operation(summary = "用户详情")
    public Result<UserResponse> detail(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @OperLog(title = "用户管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(sysUserService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        sysUserService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @OperLog(title = "用户管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @OperLog(title = "用户管理", businessType = BusinessType.STATUS)
    @Operation(summary = "启用/停用用户")
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        sysUserService.changeStatus(id, request.getStatus());
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @OperLog(title = "用户管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(id, request.getPassword());
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("@ss.hasPermi('system:user:assignRole')")
    @OperLog(title = "用户管理", businessType = BusinessType.GRANT)
    @Operation(summary = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody IdListRequest request) {
        sysUserService.assignRoles(id, request.getIds());
        return Result.success();
    }

    @PutMapping("/{id}/posts")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @OperLog(title = "用户管理", businessType = BusinessType.GRANT)
    @Operation(summary = "分配岗位")
    public Result<Void> assignPosts(@PathVariable Long id, @Valid @RequestBody IdListRequest request) {
        sysUserService.assignPosts(id, request.getIds());
        return Result.success();
    }
}
