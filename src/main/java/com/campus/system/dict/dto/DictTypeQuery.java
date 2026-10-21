package com.campus.system.dict.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQuery extends PageQuery {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "id",
            "dictName", "dict_name",
            "dictType", "dict_type",
            "createTime", "create_time"
    );

    private String dictName;
    private String dictType;
    private Integer status;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
