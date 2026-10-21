package com.campus.system.log.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLog {

    private Long id;
    private String username;
    private Long userId;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private Integer success;
    private String message;
    private LocalDateTime loginTime;
}
