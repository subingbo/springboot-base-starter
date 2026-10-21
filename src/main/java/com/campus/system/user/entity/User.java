package com.campus.system.user.entity;

import com.campus.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    private Long id;
    private Long deptId;
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
    private Integer status;
    private Integer tokenVersion;
    private String loginIp;
    private LocalDateTime loginTime;

    /**
     * 关联查询字段，不参与持久化。
     */
    private String deptName;
}
