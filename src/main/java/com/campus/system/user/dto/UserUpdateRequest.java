package com.campus.system.user.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {

    @Size(max = 64, message = "昵称不能超过 64 个字符")
    @SafeText
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过 128 个字符")
    private String email;

    @Size(max = 20, message = "手机号不能超过 20 个字符")
    private String phone;

    @Min(value = 0, message = "性别取值非法")
    @Max(value = 2, message = "性别取值非法")
    private Integer sex;

    private Long deptId;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;

    private List<Long> roleIds;
    private List<Long> postIds;

    @NotNull(message = "版本号不能为空")
    private Integer version;
}
