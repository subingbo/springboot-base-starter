package com.campus.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResultCode implements ErrorCode {

    SUCCESS(200, "success", HttpStatus.OK),
    BAD_REQUEST(400, "参数校验失败", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "未登录", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "无权限", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "资源不存在", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(405, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(409, "数据冲突", HttpStatus.CONFLICT),
    PAYLOAD_TOO_LARGE(413, "请求内容过大", HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的请求类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    LOCKED(423, "资源已锁定", HttpStatus.LOCKED),
    TOO_MANY_REQUESTS(429, "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR(500, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
