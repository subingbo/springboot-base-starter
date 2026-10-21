package com.campus.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    REPEAT_SUBMIT(40001, "请勿重复提交", HttpStatus.CONFLICT),
    INVALID_ARGUMENT(40002, "参数不合法", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FIELD(40003, "非法的排序字段", HttpStatus.BAD_REQUEST),
    RATE_LIMITED(40004, "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    OPERATION_FAILED(40005, "操作失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
