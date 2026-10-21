package com.campus;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.dto.TokenResponse;
import com.campus.common.dto.IdListRequest;
import com.campus.system.dept.dto.DeptRequest;
import com.campus.system.role.dto.RoleRequest;
import com.campus.system.user.dto.UserCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestContainers.MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", TestContainers.MYSQL::getUsername);
        registry.add("spring.datasource.password", TestContainers.MYSQL::getPassword);
        registry.add("spring.data.redis.host", TestContainers.REDIS::getHost);
        registry.add("spring.data.redis.port", () -> TestContainers.REDIS.getMappedPort(6379).toString());
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${campus.jwt.secret}")
    protected String jwtSecret;

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected String bearer(TokenResponse tokens) {
        return "Bearer " + tokens.getAccessToken();
    }

    protected void register(String username, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setNickname("测试用户");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    protected TokenResponse login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = body(result).path("data");
        return new TokenResponse(
                data.path("accessToken").asText(),
                data.path("refreshToken").asText(),
                data.path("expiresIn").asLong()
        );
    }

    protected TokenResponse adminLogin() throws Exception {
        return login("admin", "admin123");
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode getJson(String url, String bearer) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return body(result);
    }

    protected JsonNode postJson(String url, String bearer, Object request) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return body(result);
    }

    protected JsonNode putJson(String url, String bearer, Object request) throws Exception {
        MvcResult result = mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return body(result);
    }

    protected Long createDept(String bearer, String name) throws Exception {
        DeptRequest request = new DeptRequest();
        request.setParentId(0L);
        request.setName(name);
        request.setSort(0);
        request.setStatus(1);
        return postJson("/api/system/depts", bearer, request).path("data").asLong();
    }

    protected Long createRole(String bearer, String code, int dataScope) throws Exception {
        RoleRequest request = new RoleRequest();
        request.setName("数据权限角色");
        request.setCode(code.toUpperCase(Locale.ROOT));
        request.setSort(1);
        request.setDataScope(dataScope);
        request.setStatus(1);
        return postJson("/api/system/roles", bearer, request).path("data").asLong();
    }

    protected Long createUser(String bearer, String username, Long deptId, List<Long> roleIds) throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(username);
        request.setPassword("Password1");
        request.setNickname("测试用户");
        request.setDeptId(deptId);
        request.setRoleIds(roleIds);
        request.setSex(0);
        request.setStatus(1);
        return postJson("/api/system/users", bearer, request).path("data").asLong();
    }

    protected void assignMenus(String bearer, Long roleId, List<Long> menuIds) throws Exception {
        IdListRequest request = new IdListRequest();
        request.setIds(menuIds);
        putJson("/api/system/roles/" + roleId + "/menus", bearer, request);
    }

    protected String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    protected boolean hasTotal(String url, String bearer) {
        try {
            return getJson(url, bearer).path("data").path("total").asLong() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    protected void await(BooleanSupplier condition, String message) {
        for (int i = 0; i < 40; i++) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (Exception ignored) {
                // retry
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertThat(false).as(message).isTrue();
    }
}
