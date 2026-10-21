package com.campus.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private TokenResponse response;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;
}
