package com.campus.file.controller;

import com.campus.common.annotation.RateLimit;
import com.campus.common.result.Result;
import com.campus.file.dto.FileResponse;
import com.campus.file.service.FileStorageService;
import com.campus.security.user.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @RateLimit(key = "file:upload", limit = 30, windowSeconds = 60, dimension = RateLimit.Dimension.USER)
    @Operation(summary = "上传文件，scene=IMAGE|AVATAR|DOC")
    public Result<FileResponse> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(defaultValue = "DOC") String scene) {
        return Result.success(fileStorageService.store(file, scene, SecurityUtils.getUserId()));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载文件")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileStorageService.download(id, response);
    }
}
