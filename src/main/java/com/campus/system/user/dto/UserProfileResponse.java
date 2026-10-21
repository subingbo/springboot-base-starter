package com.campus.system.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer sex;
    private Long deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
}
