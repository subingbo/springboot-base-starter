package com.campus.system.post.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.post.dto.PostQuery;
import com.campus.system.post.dto.PostRequest;
import com.campus.system.post.entity.Post;
import com.campus.system.post.service.PostService;
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

@Tag(name = "岗位管理")
@RestController
@RequestMapping("/api/system/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('system:post:list')")
    @Operation(summary = "岗位分页")
    public Result<PageResult<Post>> page(@Valid PostQuery query) {
        return Result.success(postService.page(query));
    }

    @GetMapping("/options")
    @Operation(summary = "启用岗位下拉数据")
    public Result<List<Post>> options() {
        return Result.success(postService.enabled());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:post:query')")
    @Operation(summary = "岗位详情")
    public Result<Post> detail(@PathVariable Long id) {
        return Result.success(postService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:post:add')")
    @OperLog(title = "岗位管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增岗位")
    public Result<Long> create(@Valid @RequestBody PostRequest request) {
        return Result.success(postService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:post:edit')")
    @OperLog(title = "岗位管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改岗位")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
        postService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('system:post:remove')")
    @OperLog(title = "岗位管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除岗位")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.success();
    }
}
