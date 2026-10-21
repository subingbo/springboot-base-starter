package com.campus.common.cache;

import java.io.Serializable;

/**
 * 空值占位符，用于缓存穿透保护；反序列化后仍能识别为“缓存了 null”。
 */
public class NullValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final NullValue INSTANCE = new NullValue();

    public NullValue() {
    }
}
