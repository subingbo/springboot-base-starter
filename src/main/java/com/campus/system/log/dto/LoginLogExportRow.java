package com.campus.system.log.dto;

import com.campus.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogExportRow {

    @Excel(name = "用户名", order = 1)
    private String username;

    @Excel(name = "IP", order = 2)
    private String ip;

    @Excel(name = "浏览器", order = 3)
    private String browser;

    @Excel(name = "操作系统", order = 4)
    private String os;

    @Excel(name = "状态", order = 5)
    private String status;

    @Excel(name = "消息", order = 6, width = 40)
    private String message;

    @Excel(name = "登录时间", order = 7, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
