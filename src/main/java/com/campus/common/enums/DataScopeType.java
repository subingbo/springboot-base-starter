package com.campus.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataScopeType {

    ALL(1),
    CUSTOM(2),
    DEPT(3),
    DEPT_AND_CHILD(4),
    SELF(5);

    private final int code;

    public static DataScopeType fromCode(Integer code) {
        if (code == null) {
            return SELF;
        }
        for (DataScopeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return SELF;
    }
}
