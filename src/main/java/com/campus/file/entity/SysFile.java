package com.campus.file.entity;

import com.campus.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysFile extends BaseEntity {

    private Long id;
    private String originalName;
    private String storedName;
    private String storedPath;
    private String url;
    private Long size;
    private String contentType;
    private String sha256;
    private String scene;
    private Long uploaderId;
}
