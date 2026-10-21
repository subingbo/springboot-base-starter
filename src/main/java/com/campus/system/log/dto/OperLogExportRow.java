package com.campus.system.log.dto;

import com.campus.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLogExportRow {

    @Excel(name = "操作模块", order = 1)
    private String title;

    @Excel(name = "业务类型", order = 2)
    private String businessType;

    @Excel(name = "操作人", order = 3)
    private String operName;

    @Excel(name = "IP", order = 4)
    private String operIp;

    @Excel(name = "请求地址", order = 5, width = 40)
    private String operUrl;

    @Excel(name = "状态", order = 6)
    private String status;

    @Excel(name = "耗时(ms)", order = 7)
    private Long costTime;

    @Excel(name = "操作时间", order = 8, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    @Excel(name = "请求参数", order = 9, width = 50)
    private String operParam;
}
