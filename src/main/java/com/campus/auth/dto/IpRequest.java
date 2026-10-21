package com.campus.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IpRequest {

    @NotBlank(message = "IP 不能为空")
    @Pattern(regexp = "^[0-9a-fA-F:.%\\[\\]]{1,64}$", message = "IP 格式不正确")
    private String ip;
}
