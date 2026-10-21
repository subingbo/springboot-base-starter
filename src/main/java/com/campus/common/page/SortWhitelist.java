package com.campus.common.page;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.CommonErrorCode;

import java.util.Map;

public final class SortWhitelist {

    private SortWhitelist() {
    }

    public static String resolve(String orderBy, Boolean asc, Map<String, String> allowedColumns) {
        if (orderBy == null || orderBy.isBlank()) {
            return null;
        }
        String column = allowedColumns.get(orderBy);
        if (column == null) {
            throw new BusinessException(CommonErrorCode.INVALID_SORT_FIELD);
        }
        return column + (Boolean.FALSE.equals(asc) ? " DESC" : " ASC");
    }
}
