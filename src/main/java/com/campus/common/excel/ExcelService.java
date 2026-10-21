package com.campus.common.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.campus.common.annotation.Excel;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.CommonErrorCode;
import com.campus.common.result.FileErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    private static final int MAX_PAGES = 1000;

    static {
        // 防止 XLSX 解压炸弹
        ZipSecureFile.setMinInflateRatio(0.01);
        ZipSecureFile.setMaxEntrySize(200L * 1024 * 1024);
    }

    private final DictTranslator dictTranslator;

    public void write(HttpServletResponse response, String filename, String sheetName,
                      Class<?> clazz, List<?> data) {
        List<List<String>> head = buildHead(clazz);
        List<List<Object>> rows = toRows(clazz, data);
        prepareResponse(response, filename);
        try {
            EasyExcel.write(response.getOutputStream()).head(head).sheet(sheetName).doWrite(rows);
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED, "导出失败");
        }
    }

    /**
     * 按页加载并写入，避免一次性把大数据集加载进内存。
     */
    public void writeBatches(HttpServletResponse response, String filename, String sheetName,
                             Class<?> clazz, IntFunction<List<?>> loader, int pageSize) {
        List<List<String>> head = buildHead(clazz);
        prepareResponse(response, filename);
        int size = Math.max(1, pageSize);
        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream()).head(head).build()) {
            WriteSheet sheet = EasyExcel.writerSheet(sheetName).build();
            for (int page = 1; page <= MAX_PAGES; page++) {
                List<?> batch = loader.apply(page);
                if (batch == null || batch.isEmpty()) {
                    break;
                }
                writer.write(toRows(clazz, batch), sheet);
                if (batch.size() < size) {
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("excel export failed", ex);
            throw new BusinessException(FileErrorCode.STORE_FAILED, "导出失败");
        }
    }

    public void writeTemplate(HttpServletResponse response, String filename, String sheetName,
                              Class<?> clazz) {
        prepareResponse(response, filename);
        try {
            EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(List.of());
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED, "模板生成失败");
        }
    }

    public <T> List<T> read(MultipartFile file, Class<T> clazz, int maxRows) {
        ExcelReadListener<T> listener = new ExcelReadListener<>(maxRows);
        try (InputStream input = file.getInputStream()) {
            EasyExcel.read(input, clazz, listener)
                    .headRowNumber(1)
                    .autoTrim(true)
                    .sheet()
                    .doRead();
            return listener.getData();
        } catch (BusinessException ex) {
            throw ex;
        } catch (ExcelAnalysisException ex) {
            if (ex.getCause() instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(CommonErrorCode.INVALID_ARGUMENT, "Excel 解析失败");
        } catch (IOException ex) {
            throw new BusinessException(FileErrorCode.STORE_FAILED, "Excel 读取失败");
        }
    }

    private void prepareResponse(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }

    private List<List<String>> buildHead(Class<?> clazz) {
        List<ExcelField> fields = excelFields(clazz);
        List<List<String>> head = new ArrayList<>();
        for (ExcelField excelField : fields) {
            head.add(List.of(excelField.annotation().name()));
        }
        return head;
    }

    private List<List<Object>> toRows(Class<?> clazz, List<?> data) {
        List<ExcelField> fields = excelFields(clazz);
        List<List<Object>> rows = new ArrayList<>(data.size());
        for (Object item : data) {
            List<Object> row = new ArrayList<>(fields.size());
            for (ExcelField excelField : fields) {
                row.add(valueOf(item, excelField));
            }
            rows.add(row);
        }
        return rows;
    }

    private Object valueOf(Object item, ExcelField excelField) {
        try {
            excelField.field().setAccessible(true);
            Object value = excelField.field().get(item);
            if (value == null) {
                return "";
            }
            String dictType = excelField.annotation().dictType();
            if (!dictType.isBlank()) {
                return sanitize(dictTranslator.translate(dictType, value));
            }
            if (value instanceof LocalDateTime dateTime) {
                return dateTime.format(DateTimeFormatter.ofPattern(excelField.annotation().dateFormat()));
            }
            if (value instanceof LocalDate date) {
                return date.format(DateTimeFormatter.ofPattern(excelField.annotation().dateFormat()));
            }
            if (value instanceof String text) {
                return sanitize(text);
            }
            return value;
        } catch (IllegalAccessException ex) {
            return "";
        }
    }

    /**
     * 防 Excel 公式注入：危险前缀加单引号，Excel 会按文本处理。
     */
    private String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    private List<ExcelField> excelFields(Class<?> clazz) {
        List<ExcelField> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Excel annotation = field.getAnnotation(Excel.class);
                if (annotation != null) {
                    fields.add(new ExcelField(field, annotation));
                }
            }
            current = current.getSuperclass();
        }
        fields.sort(Comparator.comparingInt(field -> field.annotation().order()));
        return fields;
    }

    private record ExcelField(Field field, Excel annotation) {
    }
}
