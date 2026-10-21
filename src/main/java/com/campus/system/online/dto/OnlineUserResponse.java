package com.campus.system.online.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineUserResponse {

    private String tokenId;
    private Long userId;
    private String username;
    private String nickname;
    private String ip;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
}
