package com.campus.system.user.dto;

import com.campus.common.annotation.SafeText;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,32}$", message = "用户名只能包含字母、数字和下划线，长度 3-32")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须为 8-32 位")
    private String password;

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
    private Integer sex = 0;

    private Long deptId;

    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status = 1;

    @Size(max = 500, message = "备注不能超过 500 个字符")
    @SafeText
    private String remark;

    private List<Long> roleIds;
    private List<Long> postIds;
}
