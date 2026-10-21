package com.campus.system.log.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQuery extends PageQuery {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "id",
            "loginTime", "login_time"
    );

    private String username;
    private Integer success;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
