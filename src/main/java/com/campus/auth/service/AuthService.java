package com.campus.auth.service;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.OnlineSession;
import com.campus.auth.dto.RefreshRequest;
import com.campus.auth.dto.RefreshResult;
import com.campus.auth.dto.RefreshSession;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.dto.TokenResponse;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.AuthErrorCode;
import com.campus.common.result.ResultCode;
import com.campus.common.util.IpUtils;
import com.campus.common.util.UserAgentUtils;
import com.campus.config.AuthProperties;
import com.campus.config.SecurityProperties;
import com.campus.security.jwt.JwtProperties;
import com.campus.security.jwt.JwtTokenProvider;
import com.campus.security.user.LoginUser;
import com.campus.security.user.SecurityUtils;
import com.campus.system.log.service.LoginLogService;
import com.campus.system.user.entity.User;
import com.campus.system.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenSessionService tokenSessionService;
    private final CaptchaService captchaService;
    private final LoginSecurityService loginSecurityService;
    private final LoginLogService loginLogService;
    private final AuthProperties authProperties;
    private final SecurityProperties securityProperties;

    public void register(RegisterRequest request) {
        if (!authProperties.isRegisterEnabled()) {
            throw new BusinessException(AuthErrorCode.REGISTER_DISABLED);
        }
        userService.createUser(request.getUsername(), request.getPassword(),
                request.getNickname(), request.getEmail(), "USER");
    }

    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = clientIp(httpRequest);
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        loginSecurityService.assertNotBlocked(ip, request.getUsername());
        captchaService.validateIfEnabled(request.getCaptchaId(), request.getCaptchaCode());

        User user = userService.getByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginSecurityService.recordFailure(request.getUsername());
            loginLogService.recordAsync(request.getUsername(), user == null ? null : user.getId(),
                    ip, userAgent, false, "用户名或密码错误");
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLogService.recordAsync(request.getUsername(), user.getId(), ip, userAgent, false, "账号已禁用");
            throw new BusinessException(AuthErrorCode.ACCOUNT_DISABLED);
        }

        loginSecurityService.clearFailure(request.getUsername());
        LoginUser loginUser = userService.loadLoginUser(user.getId());
        String familyId = jwtTokenProvider.newTokenId();
        TokenResponse response = issueTokens(loginUser, familyId, ip, userAgent);
        userService.updateLoginInfo(user.getId(), ip, LocalDateTime.now());
        loginLogService.recordAsync(request.getUsername(), user.getId(), ip, userAgent, true, "登录成功");
        return response;
    }

    public TokenResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        Claims claims = jwtTokenProvider.parseToken(request.getRefreshToken());
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        String tokenId = jwtTokenProvider.getTokenId(claims);
        String familyId = jwtTokenProvider.getFamilyId(claims);
        Long userId = jwtTokenProvider.getUserId(claims);
        int claimVersion = jwtTokenProvider.getTokenVersion(claims);
        if (!StringUtils.hasText(tokenId) || !StringUtils.hasText(familyId)) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        String ip = clientIp(httpRequest);
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);

        RefreshSession session = tokenSessionService.consumeRefreshSession(tokenId);
        if (session == null) {
            RefreshResult cached = tokenSessionService.getRefreshResult(tokenId);
            if (cached != null && authProperties.getRefreshGraceSeconds() > 0
                    && Objects.equals(cached.getIp(), ip)
                    && Objects.equals(cached.getUserAgent(), userAgent)) {
                // 并发刷新/网络重试的幂等返回，避免误判为重放。
                return cached.getResponse();
            }
            tokenSessionService.revokeFamily(familyId);
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }
        if (!Objects.equals(session.getUserId(), userId)
                || !Objects.equals(session.getFamilyId(), familyId)
                || !tokenSessionService.isFamilyMember(familyId, tokenId)) {
            tokenSessionService.revokeFamily(familyId);
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        LoginUser loginUser = userService.loadLoginUser(userId);
        int currentVersion = loginUser.getTokenVersion() == null ? 0 : loginUser.getTokenVersion();
        if (currentVersion != claimVersion) {
            tokenSessionService.revokeFamily(familyId);
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 轮换后立即失效旧 Access Token。
        tokenSessionService.blacklistToken(tokenId, jwtProperties.getAccessTokenExpiration());
        TokenResponse response = issueTokens(loginUser, familyId, ip, userAgent);
        tokenSessionService.removeFamilyMember(familyId, tokenId);
        tokenSessionService.removeOnlineSession(tokenId);
        if (tokenSessionService.isFamilyEmpty(familyId)) {
            tokenSessionService.removeUserFamily(userId, familyId);
        }
        tokenSessionService.saveRefreshResult(tokenId, response, ip, userAgent,
                authProperties.getRefreshGraceSeconds());
        return response;
    }

    public void logout(String accessToken, HttpServletRequest httpRequest) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Claims claims = jwtTokenProvider.parseToken(accessToken);
        if (jwtTokenProvider.isRefreshToken(claims)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 无效");
        }
        String tokenId = jwtTokenProvider.getTokenId(claims);
        String familyId = jwtTokenProvider.getFamilyId(claims);
        Long userId = jwtTokenProvider.getUserId(claims);
        tokenSessionService.blacklistToken(tokenId, jwtTokenProvider.getRemainingSeconds(claims));
        tokenSessionService.deleteRefreshSession(tokenId);
        tokenSessionService.removeOnlineSession(tokenId);
        if (familyId != null) {
            tokenSessionService.removeFamilyMember(familyId, tokenId);
            if (tokenSessionService.isFamilyEmpty(familyId)) {
                tokenSessionService.removeUserFamily(userId, familyId);
            }
        }
    }

    public void logoutAll() {
        userService.forceLogout(SecurityUtils.getUserId(), true);
    }

    private TokenResponse issueTokens(LoginUser loginUser, String familyId,
                                      String ip, String userAgent) {
        String tokenId = jwtTokenProvider.newTokenId();
        int version = loginUser.getTokenVersion() == null ? 0 : loginUser.getTokenVersion();
        String accessToken = jwtTokenProvider.generateAccessToken(
                loginUser.getId(), loginUser.getUsername(), tokenId, familyId, version);
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                loginUser.getId(), loginUser.getUsername(), tokenId, familyId, version);

        LocalDateTime now = LocalDateTime.now();
        RefreshSession session = new RefreshSession(
                tokenId, loginUser.getId(), familyId, ip, userAgent, now);
        tokenSessionService.saveRefreshSession(session);

        OnlineSession onlineSession = new OnlineSession(
                tokenId, loginUser.getId(), loginUser.getUsername(), loginUser.getNickname(),
                familyId, ip, UserAgentUtils.browser(userAgent), UserAgentUtils.os(userAgent), now);
        tokenSessionService.saveOnlineSession(onlineSession);
        tokenSessionService.cacheLoginUser(loginUser, jwtProperties.getAccessTokenExpiration());
        return new TokenResponse(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
    }

    private String clientIp(HttpServletRequest request) {
        return IpUtils.getClientIp(request, securityProperties.isTrustProxyHeaders());
    }
}
