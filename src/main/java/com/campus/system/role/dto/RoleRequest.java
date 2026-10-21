package com.campus.system.role.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过 64 个字符")
    @SafeText
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,31}$", message = "角色编码必须为大写字母、数字或下划线，长度 2-32")
    private String code;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sort = 0;

    @NotNull(message = "数据范围不能为空")
    @Min(value = 1, message = "数据范围取值非法")
    @Max(value = 5, message = "数据范围取值非法")
    private Integer dataScope = 5;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
