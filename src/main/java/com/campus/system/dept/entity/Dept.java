package com.campus.system.dept.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Dept extends BaseEntity {

    private Long id;
    private Long parentId;
    private String ancestors;
    private String name;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
