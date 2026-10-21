package com.campus.system.menu.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String perms;
    private String type;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
