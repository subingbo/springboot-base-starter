package com.campus.system.role.dto;

import com.campus.system.role.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleResponse {

    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private Integer dataScope;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Long> menuIds;
    private List<Long> deptIds;

    public static RoleResponse from(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setCode(role.getCode());
        response.setSort(role.getSort());
        response.setDataScope(role.getDataScope());
        response.setStatus(role.getStatus());
        response.setRemark(role.getRemark());
        response.setCreateTime(role.getCreateTime());
        return response;
    }
}
