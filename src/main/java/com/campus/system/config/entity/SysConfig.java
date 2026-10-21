package com.campus.system.config.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {

    private Long id;
    private String configName;
    private String configKey;
    private String configValue;
}
