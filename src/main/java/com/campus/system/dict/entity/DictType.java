package com.campus.system.dict.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictType extends BaseEntity {

    private Long id;
    private String dictName;
    private String dictType;
    private Integer status;
}
