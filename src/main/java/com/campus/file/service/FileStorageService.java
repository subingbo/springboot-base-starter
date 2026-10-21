package com.campus.file.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.FileErrorCode;
import com.campus.config.FileProperties;
import com.campus.config.SecurityProperties;
import com.campus.file.dto.FileResponse;
import com.campus.file.entity.SysFile;
import com.campus.file.mapper.SysFileMapper;
import com.campus.file.util.FileTypeUtils;
import com.campus.security.user.SecurityUtils;
import com.campus.system.config.service.SysConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileProperties fileProperties;
    private final SysConfigService sysConfigService;
    private final SysFileMapper sysFileMapper;
    private final SecurityProperties securityProperties;

    private Path root;

    @PostConstruct
    public void init() {
        this.root = Paths.get(fileProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("cannot create upload directory: " + root, ex);
        }
    }

    public FileResponse store(MultipartFile file, String sceneValue, Long uploaderId) {
        FileScene scene = FileScene.from(sceneValue);
        int maxMb = sysConfigService.getInt("sys.file.maxSizeMb", fileProperties.getMaxSizeMb());
        FileTypeUtils.FileInfo info = FileTypeUtils.validate(
                file, scene, maxMb, fileProperties.getImageMaxPixels());

        String datePath = LocalDate.now().format(DATE_PATH);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + info.extension();
        Path directory = root.resolve(datePath).normalize();
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(FileErrorCode.PATH_INVALID);
        }
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED);
        }

        String relativePath = root.relativize(target).toString().replace('\\', '/');
        String sha256 = sha256(target);
        long size;
        try {
            size = Files.size(target);
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED);
        }

        SysFile entity = new SysFile();
        entity.setOriginalName(sanitizeFilename(file.getOriginalFilename(), info.extension()));
        entity.setStoredName(storedName);
        entity.setStoredPath(relativePath);
        entity.setSize(size);
        entity.setContentType(info.contentType());
        entity.setSha256(sha256);
        entity.setScene(scene.name());
        entity.setUploaderId(uploaderId);
        sysFileMapper.insert(entity);

        String url = (scene == FileScene.IMAGE || scene == FileScene.AVATAR)
                ? fileProperties.getPublicUrlPrefix() + "/" + relativePath
                : "/api/files/" + entity.getId() + "/download";
        sysFileMapper.updateUrl(entity.getId(), url);
        return new FileResponse(entity.getId(), entity.getOriginalName(), url, size, info.contentType());
    }

    public SysFile getFile(Long id) {
        SysFile file = sysFileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        return file;
    }

    public void download(Long id, HttpServletResponse response) {
        SysFile file = getFile(id);
        if (!canAccess(file)) {
            throw new BusinessException(FileErrorCode.DOWNLOAD_FORBIDDEN);
        }
        Path path = resolve(file);
        if (!Files.exists(path)) {
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        try {
            long size = Files.size(path);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(size);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                            .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                            .build()
                            .toString());
            response.setHeader("X-Content-Type-Options", "nosniff");
            try (OutputStream output = response.getOutputStream()) {
                Files.copy(path, output);
            }
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED);
        }
    }

    public Path resolve(SysFile file) {
        Path path = root.resolve(file.getStoredPath()).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException(FileErrorCode.PATH_INVALID);
        }
        return path;
    }

    private boolean canAccess(SysFile file) {
        Long userId = SecurityUtils.getUserIdOrNull();
        return userId != null
                && (userId.equals(file.getUploaderId())
                || SecurityUtils.isAdmin(securityProperties.getAdminRoleCode()));
    }

    private String sanitizeFilename(String originalName, String extension) {
        String name = originalName == null ? "file." + extension : originalName;
        name = name.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        if (!StringUtils.hasText(name)) {
            name = "file." + extension;
        }
        return name.length() > 200 ? name.substring(0, 200) : name;
    }

    private String sha256(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED);
        }
    }
}
