package com.campus.file.util;

import com.campus.common.exception.BusinessException;
import com.campus.common.result.FileErrorCode;
import com.campus.file.service.FileScene;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileTypeUtils {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> DOC_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "zip"
    );
    private static final int MAX_ZIP_ENTRIES = 2000;
    private static final long MAX_UNCOMPRESSED_BYTES = 200L * 1024 * 1024;

    private FileTypeUtils() {
    }

    public record FileInfo(String extension, String contentType) {
    }

    public static FileInfo validate(MultipartFile file, FileScene scene, int maxSizeMb, long maxPixels) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileErrorCode.EMPTY_FILE);
        }
        long maxBytes = Math.max(1, maxSizeMb) * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(FileErrorCode.FILE_TOO_LARGE);
        }
        String extension = extensionOf(file.getOriginalFilename());
        Set<String> allowed = scene == FileScene.DOC ? DOC_EXTENSIONS : IMAGE_EXTENSIONS;
        if (!allowed.contains(extension)) {
            throw new BusinessException(FileErrorCode.TYPE_NOT_ALLOWED);
        }
        byte[] header = readHeader(file);
        switch (extension) {
            case "jpg", "jpeg" -> {
                require(isJpeg(header));
                validateImage(file, "jpeg", maxPixels);
            }
            case "png" -> {
                require(isPng(header));
                validateImage(file, "png", maxPixels);
            }
            case "gif" -> {
                require(isGif(header));
                validateImage(file, "gif", maxPixels);
            }
            case "pdf" -> require(isPdf(header));
            case "doc", "xls", "ppt" -> require(isOle2(header));
            case "docx" -> validateZip(file, "docx");
            case "xlsx" -> validateZip(file, "xlsx");
            case "pptx" -> validateZip(file, "pptx");
            case "zip" -> validateZip(file, "zip");
            case "txt", "md", "csv" -> require(isText(file));
            default -> throw new BusinessException(FileErrorCode.TYPE_NOT_ALLOWED);
        }
        return new FileInfo(extension, contentType(extension));
    }

    public static void validateExcel(MultipartFile file, int maxSizeMb) {
        FileInfo info = validate(file, FileScene.DOC, maxSizeMb, Long.MAX_VALUE);
        if (!"xlsx".equals(info.extension())) {
            throw new BusinessException(FileErrorCode.TYPE_NOT_ALLOWED, "仅支持 .xlsx 文件");
        }
    }

    public static String extensionOf(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        String name = filename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static String contentType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            case "txt", "md", "csv" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private static byte[] readHeader(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return input.readNBytes(8);
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED);
        }
    }

    private static void validateImage(MultipartFile file, String expectedFormat, long maxPixels) {
        try (InputStream input = file.getInputStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.contains(expectedFormat)) {
                    throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > maxPixels) {
                    throw new BusinessException(FileErrorCode.IMAGE_TOO_LARGE);
                }
            } finally {
                reader.dispose();
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
        }
    }

    private static void validateZip(MultipartFile file, String expectedType) {
        boolean contentTypes = false;
        boolean word = false;
        boolean excel = false;
        boolean ppt = false;
        int entries = 0;
        long total = 0;
        byte[] buffer = new byte[8192];
        Path temp = null;
        try {
            // EasyExcel/POI 生成的 OOXML 使用数据描述符，JDK ZipInputStream 会误报 size=0；
            // 先落临时文件，再通过中央目录读取，兼容性更好。
            temp = Files.createTempFile("campus-zip-", ".tmp");
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            try (ZipFile zip = new ZipFile(temp.toFile())) {
                Enumeration<? extends ZipEntry> enumeration = zip.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry entry = enumeration.nextElement();
                    entries++;
                    if (entries > MAX_ZIP_ENTRIES) {
                        throw new BusinessException(FileErrorCode.CONTENT_MISMATCH, "压缩包条目过多");
                    }
                    String name = entry.getName();
                    if (name.startsWith("/") || name.contains("..")) {
                        throw new BusinessException(FileErrorCode.CONTENT_MISMATCH, "压缩包路径非法");
                    }
                    if ("[Content_Types].xml".equals(name)) {
                        contentTypes = true;
                    }
                    if (name.startsWith("word/")) {
                        word = true;
                    }
                    if (name.startsWith("xl/")) {
                        excel = true;
                    }
                    if (name.startsWith("ppt/")) {
                        ppt = true;
                    }
                    try (InputStream entryInput = zip.getInputStream(entry)) {
                        int read;
                        while ((read = entryInput.read(buffer)) != -1) {
                            total += read;
                            if (total > MAX_UNCOMPRESSED_BYTES) {
                                throw new BusinessException(FileErrorCode.FILE_TOO_LARGE, "压缩包解压后过大");
                            }
                        }
                    }
                }
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            }
        }
        if ("zip".equals(expectedType)) {
            return;
        }
        boolean matches = contentTypes && switch (expectedType) {
            case "docx" -> word;
            case "xlsx" -> excel;
            case "pptx" -> ppt;
            default -> false;
        };
        if (!matches) {
            throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
        }
    }

    private static boolean isText(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] bytes = input.readNBytes(4096);
            for (byte value : bytes) {
                if (value == 0) {
                    return false;
                }
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static boolean isJpeg(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] header) {
        return header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A;
    }

    private static boolean isGif(byte[] header) {
        return header.length >= 6
                && header[0] == 'G' && header[1] == 'I' && header[2] == 'F'
                && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
    }

    private static boolean isPdf(byte[] header) {
        return header.length >= 5
                && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
    }

    private static boolean isOle2(byte[] header) {
        return header.length >= 8
                && (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF
                && (header[2] & 0xFF) == 0x11 && (header[3] & 0xFF) == 0xE0
                && (header[4] & 0xFF) == 0xA1 && (header[5] & 0xFF) == 0xB1
                && (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0xE1;
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new BusinessException(FileErrorCode.CONTENT_MISMATCH);
        }
    }
}
