package com.campus;

import com.alibaba.excel.EasyExcel;
import com.campus.auth.dto.TokenResponse;
import com.campus.file.dto.FileResponse;
import com.campus.system.user.dto.UserImportRow;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileExcelAcceptanceTest extends AbstractIntegrationTest {

    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=");

    @Test
    void uploadAndDownloadImage() throws Exception {
        TokenResponse admin = adminLogin();
        FileResponse file = upload(bearer(admin), "avatar.png", "image/png", PNG, "IMAGE");
        assertThat(file.getUrl()).startsWith("/uploads/");

        mockMvc.perform(get("/api/files/" + file.getId() + "/download")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(
                        result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION))
                        .contains("attachment"));
    }

    @Test
    void rejectFakeImageExtension() throws Exception {
        TokenResponse admin = adminLogin();
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg", "not-an-image".getBytes());
        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("scene", "IMAGE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(30003));
    }

    @Test
    void sanitizePathTraversalFilename() throws Exception {
        TokenResponse admin = adminLogin();
        FileResponse file = upload(bearer(admin), "../evil.png", "image/png", PNG, "IMAGE");
        assertThat(file.getOriginalName()).isEqualTo("evil.png");
    }

    @Test
    void exportUsersAndTemplate() throws Exception {
        TokenResponse admin = adminLogin();
        MvcResult export = mockMvc.perform(get("/api/system/users/export")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(export.getResponse().getContentAsByteArray()).isNotEmpty();
        assertThat(export.getResponse().getContentType()).contains("spreadsheetml");

        MvcResult template = mockMvc.perform(get("/api/system/users/import-template")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(template.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    void importUsersFromExcel() throws Exception {
        TokenResponse admin = adminLogin();
        String suffix = String.valueOf(System.nanoTime());

        UserImportRow first = new UserImportRow();
        first.setUsername("imp1_" + suffix);
        first.setPassword("Import1234");
        first.setNickname("导入用户1");
        first.setStatus(1);
        first.setRoleCodes("USER");

        UserImportRow second = new UserImportRow();
        second.setUsername("imp2_" + suffix);
        second.setPassword("Import1234");
        second.setNickname("导入用户2");
        second.setStatus(1);
        second.setRoleCodes("USER");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EasyExcel.write(output, UserImportRow.class).sheet("用户").doWrite(List.of(first, second));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                output.toByteArray()
        );
        mockMvc.perform(multipart("/api/system/users/import")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(2));
    }

    private FileResponse upload(String bearer, String filename, String contentType,
                                byte[] content, String scene) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
        MvcResult result = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("scene", scene)
                        .header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = body(result).path("data");
        return new FileResponse(
                data.path("id").asLong(),
                data.path("originalName").asText(),
                data.path("url").asText(),
                data.path("size").asLong(),
                data.path("contentType").asText()
        );
    }
}
