package com.campus.system.dict.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictDataRequest {

    @NotBlank(message = "字典类型不能为空")
    @Size(max = 64, message = "字典类型不能超过 64 个字符")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 64, message = "字典标签不能超过 64 个字符")
    @SafeText
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 64, message = "字典值不能超过 64 个字符")
    private String dictValue;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer dictSort = 0;

    @Size(max = 64, message = "样式类名不能超过 64 个字符")
    private String cssClass;

    @Min(value = 0, message = "默认标记取值非法")
    @Max(value = 1, message = "默认标记取值非法")
    private Integer isDefault = 0;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
