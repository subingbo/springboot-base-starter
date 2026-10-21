package com.campus.system.log.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {

    private Long id;
    private String title;
    private String businessType;
    private String method;
    private String requestMethod;
    private Long operUserId;
    private String operName;
    private String operUrl;
    private String operIp;
    private String operLocation;
    private String operParam;
    private String jsonResult;
    private Integer success;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime operTime;
}
