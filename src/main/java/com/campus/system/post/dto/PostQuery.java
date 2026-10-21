package com.campus.system.post.dto;

import com.campus.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostQuery extends PageQuery {

    private static final Map<String, String> SORTABLE = Map.of(
            "id", "id",
            "code", "code",
            "name", "name",
            "sort", "sort"
    );

    private String name;
    private String code;
    private Integer status;

    public String toOrderBy() {
        return resolveOrderBy(SORTABLE);
    }
}
