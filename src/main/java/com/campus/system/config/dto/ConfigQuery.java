package com.campus.system.config.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigQuery extends PageQuery {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "id",
            "configName", "config_name",
            "configKey", "config_key",
            "createTime", "create_time"
    );

    private String configName;
    private String configKey;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
