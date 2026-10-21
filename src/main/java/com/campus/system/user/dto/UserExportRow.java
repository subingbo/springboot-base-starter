package com.campus.system.user.dto;

import com.campus.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserExportRow {

    @Excel(name = "用户名", order = 1)
    private String username;

    @Excel(name = "昵称", order = 2)
    private String nickname;

    @Excel(name = "部门", order = 3)
    private String deptName;

    @Excel(name = "邮箱", order = 4)
    private String email;

    @Excel(name = "手机号", order = 5)
    private String phone;

    @Excel(name = "性别", order = 6, dictType = "sys_user_sex")
    private Integer sex;

    @Excel(name = "状态", order = 7, dictType = "sys_common_status")
    private Integer status;

    @Excel(name = "创建时间", order = 8, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
