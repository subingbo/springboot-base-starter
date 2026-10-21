package com.campus.system.post.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {

    private Long id;
    private String code;
    private String name;
    private Integer sort;
    private Integer status;
}
