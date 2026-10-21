package com.campus.file.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.FileErrorCode;

import java.util.Locale;

public enum FileScene {
    IMAGE,
    AVATAR,
    DOC;

    public static FileScene from(String value) {
        if (value == null || value.isBlank()) {
            return DOC;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(FileErrorCode.INVALID_SCENE);
        }
    }
}
