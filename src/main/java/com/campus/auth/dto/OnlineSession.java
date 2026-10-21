package com.campus.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tokenId;
    private Long userId;
    private String username;
    private String nickname;
    private String familyId;
    private String ip;
    private String browser;
    private String os;
    private LocalDateTime loginTime;
}
