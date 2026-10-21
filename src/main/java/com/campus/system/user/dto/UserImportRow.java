package com.campus.system.user.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserImportRow {

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("部门ID")
    private Long deptId;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("角色编码")
    private String roleCodes;
}
