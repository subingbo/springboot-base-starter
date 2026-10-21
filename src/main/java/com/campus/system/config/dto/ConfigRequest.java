package com.campus.system.config.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigRequest {

    @NotBlank(message = "参数名称不能为空")
    @Size(max = 64, message = "参数名称不能超过 64 个字符")
    @SafeText
    private String configName;

    @NotBlank(message = "参数键名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_.-]{1,127}$", message = "参数键名格式不正确")
    private String configKey;

    @NotBlank(message = "参数值不能为空")
    @Size(max = 2000, message = "参数值不能超过 2000 个字符")
    private String configValue;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;
}
