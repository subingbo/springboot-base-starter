package com.campus.common.datascope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限规则。type 使用字符串，便于 MyBatis XML 用 OGNL 判断。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private Long deptId;
    private Long userId;
    private String ancestorsPrefix;
    private List<Long> deptIds;
}
