package com.campus.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, traceId());
    }

    public static <T> Result<T> error() {
        return error(ResultCode.INTERNAL_ERROR);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, traceId());
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, traceId());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null, traceId());
    }

    public static <T> Result<T> error(int code, String message, Object data) {
        @SuppressWarnings("unchecked")
        T typed = (T) data;
        return new Result<>(code, message, typed, traceId());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message, Object data) {
        return error(errorCode.getCode(), message, data);
    }

    public static <T> Result<T> of(ErrorCode errorCode) {
        return error(errorCode);
    }

    private static String traceId() {
        return MDC.get("traceId");
    }
}
