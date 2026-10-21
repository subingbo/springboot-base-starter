package com.campus.common.result;

import org.springframework.http.HttpStatus;

/**
 * 统一错误码契约。通用错误沿用 HTTP 状态码，业务错误使用分段编码。
 */
public interface ErrorCode {

    int getCode();

    String getMessage();

    HttpStatus getHttpStatus();
}
