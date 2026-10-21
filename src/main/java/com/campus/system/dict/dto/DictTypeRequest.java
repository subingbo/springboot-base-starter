package com.campus.system.dict.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictTypeRequest {

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称不能超过 64 个字符")
    @SafeText
    private String dictName;

    @NotBlank(message = "字典类型不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{1,63}$", message = "字典类型必须以字母开头，长度 2-64")
    private String dictType;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
