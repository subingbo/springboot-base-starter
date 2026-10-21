package com.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.file")
public class FileProperties {

    private String uploadDir = "./uploads";

    private int maxSizeMb = 10;

    private long imageMaxPixels = 25_000_000L;

    private String publicUrlPrefix = "/uploads";

    private int importMaxRows = 5000;
}
