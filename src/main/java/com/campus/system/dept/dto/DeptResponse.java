package com.campus.system.dept.dto;

import com.campus.system.dept.entity.Dept;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeptResponse {

    private Long id;
    private Long parentId;
    private String ancestors;
    private String name;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
    private List<DeptResponse> children = new ArrayList<>();

    public static DeptResponse from(Dept dept) {
        DeptResponse response = new DeptResponse();
        response.setId(dept.getId());
        response.setParentId(dept.getParentId());
        response.setAncestors(dept.getAncestors());
        response.setName(dept.getName());
        response.setLeader(dept.getLeader());
        response.setPhone(dept.getPhone());
        response.setEmail(dept.getEmail());
        response.setSort(dept.getSort());
        response.setStatus(dept.getStatus());
        return response;
    }
}
