package com.campus.common.page;

import com.campus.common.exception.BusinessException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;

@Data
public class PageQuery {

    @Min(value = 1, message = "页码必须大于 0")
    private int pageNum = 1;

    @Min(value = 1, message = "每页条数必须大于 0")
    @Max(value = 100, message = "每页条数不能超过 100")
    private int pageSize = 10;

    private String orderBy;

    private Boolean isAsc = false;

    /**
     * 只允许白名单中的排序字段，防止 ORDER BY 注入。
     */
    public String resolveOrderBy(Map<String, String> allowedColumns) {
        if (orderBy == null || orderBy.isBlank()) {
            return null;
        }
        String column = allowedColumns.get(orderBy);
        if (column == null) {
            throw new BusinessException(com.campus.common.result.CommonErrorCode.INVALID_SORT_FIELD);
        }
        return column + (Boolean.FALSE.equals(isAsc) ? " DESC" : " ASC");
    }
}
