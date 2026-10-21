package com.campus;

import com.campus.auth.dto.RefreshRequest;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.dto.TokenResponse;
import com.campus.auth.service.LoginSecurityService;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.RateLimitException;
import com.campus.common.ratelimit.RateLimiter;
import com.campus.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAcceptanceTest extends AbstractIntegrationTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private LoginSecurityService loginSecurityService;

    @Test
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void invalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void expiredToken() throws Exception {
        Date past = new Date(System.currentTimeMillis() - 60_000);
        String token = Jwts.builder()
                .subject("1")
                .claim(JwtTokenProvider.CLAIM_USER_ID, 1L)
                .claim(JwtTokenProvider.CLAIM_USERNAME, "admin")
                .claim(JwtTokenProvider.CLAIM_TOKEN_ID, "expiredtoken001")
                .issuedAt(past)
                .expiration(past)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void registerLoginAndProfile() throws Exception {
        String username = unique("user");
        register(username, "Password1");
        TokenResponse tokens = login(username, "Password1");

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    void normalUserCannotAccessSystemApi() throws Exception {
        String username = unique("rbac");
        register(username, "Password1");
        TokenResponse tokens = login(username, "Password1");
        mockMvc.perform(get("/api/system/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanPageUsers() throws Exception {
        TokenResponse admin = adminLogin();
        MvcResult result = mockMvc.perform(get("/api/system/users?pageNum=1&pageSize=2")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = body(result).path("data");
        assertThat(data.path("total").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(data.path("list").size()).isLessThanOrEqualTo(2);
    }

    @Test
    void refreshRotationRevokesOldAccessToken() throws Exception {
        String username = unique("refresh");
        register(username, "Password1");
        TokenResponse tokens = login(username, "Password1");

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(tokens.getRefreshToken());
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = body(result).path("data");
        String newAccess = data.path("accessToken").asText();
        String newRefresh = data.path("refreshToken").asText();
        assertThat(newRefresh).isNotEqualTo(tokens.getRefreshToken());

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
                .andExpect(status().isOk());
    }

    @Test
    void refreshReplayRevokesWholeFamily() throws Exception {
        String username = unique("replay");
        register(username, "Password1");
        TokenResponse tokens = login(username, "Password1");

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(tokens.getRefreshToken());
        MvcResult first = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String newAccess = body(first).path("data").path("accessToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(10005));

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String username = unique("logout");
        register(username, "Password1");
        TokenResponse tokens = login(username, "Password1");

        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens)))
                .andExpect(status().isOk());

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(tokens.getRefreshToken());
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatSubmitIsRejected() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(unique("repeat"));
        request.setPassword("Password1");
        request.setNickname("重复提交");
        String payload = json(request);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void captchaIsDisabledInTestProfile() throws Exception {
        mockMvc.perform(get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void validationErrorsExposeFieldDetails() throws Exception {
        TokenResponse admin = adminLogin();
        mockMvc.perform(post("/api/system/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.errors.username").exists());
    }

    @Test
    void notFoundAndMethodNotAllowed() throws Exception {
        TokenResponse admin = adminLogin();
        mockMvc.perform(get("/api/not-exists")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void onlineUserCanBeForcedOffline() throws Exception {
        TokenResponse admin = adminLogin();
        String username = unique("online");
        register(username, "Password1");
        TokenResponse userTokens = login(username, "Password1");

        JsonNode online = getJson("/api/system/online-users?pageNum=1&pageSize=100", bearer(admin));
        String tokenId = null;
        for (JsonNode item : online.path("data").path("list")) {
            if (username.equals(item.path("username").asText())) {
                tokenId = item.path("tokenId").asText();
                break;
            }
        }
        assertThat(tokenId).isNotBlank();

        mockMvc.perform(delete("/api/system/online-users/" + tokenId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(userTokens)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginLogIsRecorded() throws Exception {
        TokenResponse admin = adminLogin();
        String username = unique("loginlog");
        register(username, "Password1");
        login(username, "Password1");
        await(() -> hasTotal(
                "/api/system/login-logs?pageNum=1&pageSize=10&username=" + username,
                bearer(admin)), "login log was not recorded");
    }

    @Test
    void rateLimiterBlocksAfterConfiguredLimit() {
        String key = unique("rate");
        rateLimiter.check(key, 2, Duration.ofMinutes(1));
        rateLimiter.check(key, 2, Duration.ofMinutes(1));
        assertThatThrownBy(() -> rateLimiter.check(key, 2, Duration.ofMinutes(1)))
                .isInstanceOf(RateLimitException.class);
    }

    @Test
    void accountLocksAfterConfiguredFailures() {
        String username = unique("lock");
        for (int i = 0; i < 5; i++) {
            loginSecurityService.recordFailure(username);
        }
        assertThatThrownBy(() -> loginSecurityService.assertNotBlocked("127.0.0.1", username))
                .isInstanceOf(BusinessException.class);

        loginSecurityService.clearFailure(username);
        loginSecurityService.assertNotBlocked("127.0.0.1", username);
    }
}
