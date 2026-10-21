package com.campus.system.role.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "id",
            "name", "name",
            "code", "code",
            "sort", "sort",
            "createTime", "create_time"
    );

    private String name;
    private String code;
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
