package com.campus.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private Long id;
    private String originalName;
    private String url;
    private Long size;
    private String contentType;
}
