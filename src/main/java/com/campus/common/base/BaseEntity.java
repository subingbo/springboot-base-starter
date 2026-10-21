package com.campus.common.base;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计基础字段。创建/更新时间与操作人由 AuditFieldInterceptor 自动填充。
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private Integer deleted;
    private Integer version;
}
