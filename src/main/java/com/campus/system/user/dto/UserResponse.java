package com.campus.system.user.dto;

import com.campus.system.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {

    private Long id;
    private Long deptId;
    private String deptName;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
    private Integer status;
    private String loginIp;
    private LocalDateTime loginTime;
    private LocalDateTime createTime;
    private String remark;
    private Integer version;
    private List<Long> roleIds;
    private List<Long> postIds;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setDeptId(user.getDeptId());
        response.setDeptName(user.getDeptName());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setSex(user.getSex());
        response.setAvatar(user.getAvatar());
        response.setStatus(user.getStatus());
        response.setLoginIp(user.getLoginIp());
        response.setLoginTime(user.getLoginTime());
        response.setCreateTime(user.getCreateTime());
        response.setRemark(user.getRemark());
        response.setVersion(user.getVersion());
        return response;
    }
}
