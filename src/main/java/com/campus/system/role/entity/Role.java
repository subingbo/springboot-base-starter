package com.campus.system.role.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private Integer dataScope;
    private Integer status;
}
