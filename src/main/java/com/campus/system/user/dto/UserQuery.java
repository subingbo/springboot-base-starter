package com.campus.system.user.dto;

import com.campus.common.datascope.DataScopeAware;
import com.campus.common.datascope.DataScopeFilter;
import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery implements DataScopeAware {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "u.id",
            "username", "u.username",
            "nickname", "u.nickname",
            "status", "u.status",
            "createTime", "u.create_time"
    );

    private String username;
    private String nickname;
    private Integer status;
    private Long deptId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private DataScopeFilter dataScope;

    /**
     * Excel 游标导出使用。
     */
    private Long lastId = 0L;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
