package com.campus.system.dept.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeptRequest {

    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 64, message = "部门名称不能超过 64 个字符")
    @SafeText
    private String name;

    @Size(max = 64, message = "负责人不能超过 64 个字符")
    @SafeText
    private String leader;

    @Size(max = 20, message = "联系电话不能超过 20 个字符")
    private String phone;

    @Size(max = 128, message = "邮箱不能超过 128 个字符")
    private String email;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sort = 0;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
