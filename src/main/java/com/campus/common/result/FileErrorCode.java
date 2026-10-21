package com.campus.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    EMPTY_FILE(30001, "文件不能为空", HttpStatus.BAD_REQUEST),
    TYPE_NOT_ALLOWED(30002, "不支持的文件类型", HttpStatus.BAD_REQUEST),
    CONTENT_MISMATCH(30003, "文件内容与后缀不一致", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(30004, "文件大小超出限制", HttpStatus.PAYLOAD_TOO_LARGE),
    IMAGE_TOO_LARGE(30005, "图片像素超出限制", HttpStatus.BAD_REQUEST),
    PATH_INVALID(30006, "文件路径非法", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(30007, "文件不存在", HttpStatus.NOT_FOUND),
    DOWNLOAD_FORBIDDEN(30008, "无权下载该文件", HttpStatus.FORBIDDEN),
    STORE_FAILED(30009, "文件保存失败", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_SCENE(30010, "不支持的上传场景", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
