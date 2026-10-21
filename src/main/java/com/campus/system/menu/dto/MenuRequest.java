package com.campus.system.menu.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuRequest {

    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称不能超过 64 个字符")
    @SafeText
    private String name;

    @Size(max = 255, message = "路由地址不能超过 255 个字符")
    private String path;

    @Size(max = 255, message = "组件路径不能超过 255 个字符")
    private String component;

    @Size(max = 128, message = "权限标识不能超过 128 个字符")
    private String perms;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是 M/C/F")
    private String type;

    @Size(max = 64, message = "图标不能超过 64 个字符")
    private String icon;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sort = 0;

    @Min(value = 0, message = "显示状态取值非法")
    @Max(value = 1, message = "显示状态取值非法")
    private Integer visible = 1;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
