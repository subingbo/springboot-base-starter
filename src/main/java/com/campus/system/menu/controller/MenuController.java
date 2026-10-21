package com.campus.system.menu.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.result.Result;
import com.campus.security.user.SecurityUtils;
import com.campus.system.menu.dto.MenuRequest;
import com.campus.system.menu.dto.MenuResponse;
import com.campus.system.menu.dto.RouteResponse;
import com.campus.system.menu.entity.Menu;
import com.campus.system.menu.service.MenuService;
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

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @Operation(summary = "菜单树")
    public Result<List<MenuResponse>> tree() {
        return Result.success(menuService.tree());
    }

    @GetMapping("/routes")
    @Operation(summary = "当前用户动态路由")
    public Result<List<RouteResponse>> routes() {
        return Result.success(menuService.routes(SecurityUtils.getUserId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @Operation(summary = "菜单详情")
    public Result<Menu> detail(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @OperLog(title = "菜单管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增菜单")
    public Result<Long> create(@Valid @RequestBody MenuRequest request) {
        return Result.success(menuService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @OperLog(title = "菜单管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改菜单")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        menuService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @OperLog(title = "菜单管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除菜单")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
