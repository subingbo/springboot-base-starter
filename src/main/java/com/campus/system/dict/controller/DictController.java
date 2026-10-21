package com.campus.system.dict.controller;

import com.campus.common.annotation.OperLog;
import com.campus.common.enums.BusinessType;
import com.campus.common.page.PageResult;
import com.campus.common.result.Result;
import com.campus.system.dict.dto.DictDataRequest;
import com.campus.system.dict.dto.DictTypeQuery;
import com.campus.system.dict.dto.DictTypeRequest;
import com.campus.system.dict.entity.DictData;
import com.campus.system.dict.entity.DictType;
import com.campus.system.dict.service.DictService;
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

@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/dict-types")
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @Operation(summary = "字典类型分页")
    public Result<PageResult<DictType>> pageTypes(@Valid DictTypeQuery query) {
        return Result.success(dictService.pageTypes(query));
    }

    @GetMapping("/dict-types/options")
    @Operation(summary = "启用字典类型下拉数据")
    public Result<List<DictType>> typeOptions() {
        return Result.success(dictService.enabledTypes());
    }

    @GetMapping("/dict-types/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @Operation(summary = "字典类型详情")
    public Result<DictType> typeDetail(@PathVariable Long id) {
        return Result.success(dictService.getType(id));
    }

    @PostMapping("/dict-types")
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @OperLog(title = "字典管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典类型")
    public Result<Long> createType(@Valid @RequestBody DictTypeRequest request) {
        return Result.success(dictService.createType(request));
    }

    @PutMapping("/dict-types/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @OperLog(title = "字典管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典类型")
    public Result<Void> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeRequest request) {
        dictService.updateType(id, request);
        return Result.success();
    }

    @DeleteMapping("/dict-types/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @OperLog(title = "字典管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除字典类型")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    @GetMapping("/dict-data/type/{dictType}")
    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @Operation(summary = "字典数据列表")
    public Result<List<DictData>> listData(@PathVariable String dictType) {
        return Result.success(dictService.listData(dictType));
    }

    @GetMapping("/dict-data/option/{dictType}")
    @Operation(summary = "字典下拉选项")
    public Result<List<DictData>> options(@PathVariable String dictType) {
        return Result.success(dictService.getEnabledData(dictType));
    }

    @GetMapping("/dict-data/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @Operation(summary = "字典数据详情")
    public Result<DictData> dataDetail(@PathVariable Long id) {
        return Result.success(dictService.getData(id));
    }

    @PostMapping("/dict-data")
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @OperLog(title = "字典管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增字典数据")
    public Result<Long> createData(@Valid @RequestBody DictDataRequest request) {
        return Result.success(dictService.createData(request));
    }

    @PutMapping("/dict-data/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @OperLog(title = "字典管理", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改字典数据")
    public Result<Void> updateData(@PathVariable Long id, @Valid @RequestBody DictDataRequest request) {
        dictService.updateData(id, request);
        return Result.success();
    }

    @DeleteMapping("/dict-data/{id}")
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @OperLog(title = "字典管理", businessType = BusinessType.DELETE)
    @Operation(summary = "删除字典数据")
    public Result<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return Result.success();
    }
}
