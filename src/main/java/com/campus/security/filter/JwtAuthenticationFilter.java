package com.campus.security.filter;

import com.campus.auth.service.TokenSessionService;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.Result;
import com.campus.common.result.ResultCode;
import com.campus.common.web.AnonymousAccessRegistry;
import com.campus.security.jwt.JwtProperties;
import com.campus.security.jwt.JwtTokenProvider;
import com.campus.security.user.LoginUser;
import com.campus.system.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionService tokenSessionService;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private final AnonymousAccessRegistry anonymousAccessRegistry;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (anonymousAccessRegistry.matches(request)) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/uploads/")
                || uri.startsWith("/actuator/health")
                || uri.startsWith("/actuator/info")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.equals("/doc.html")
                || uri.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        LoginUser loginUser;
        try {
            loginUser = authenticate(token);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.debug("jwt authentication failed uri={} reason={}", request.getRequestURI(), ex.getMessage());
            writeUnauthorized(response, ex instanceof BusinessException be
                    ? be.getMessage() : ResultCode.UNAUTHORIZED.getMessage());
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            // 只把业务调用放在认证流程之外，避免 Controller 异常被误判为 401。
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private LoginUser authenticate(String token) {
        Claims claims = jwtTokenProvider.parseToken(token);
        if (jwtTokenProvider.isRefreshToken(claims)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 无效");
        }
        String tokenId = jwtTokenProvider.getTokenId(claims);
        if (!StringUtils.hasText(tokenId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "Token 无效");
        }
        if (tokenSessionService.isTokenBlacklisted(tokenId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "登录状态已失效");
        }
        String familyId = jwtTokenProvider.getFamilyId(claims);
        if (tokenSessionService.isFamilyBlacklisted(familyId)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "登录状态已失效");
        }
        Long userId = jwtTokenProvider.getUserId(claims);
        LoginUser loginUser = tokenSessionService.getLoginUser(userId);
        if (loginUser == null) {
            loginUser = userService.loadLoginUser(userId);
            tokenSessionService.cacheLoginUser(loginUser, jwtProperties.getAccessTokenExpiration());
        }
        int currentVersion = loginUser.getTokenVersion() == null ? 0 : loginUser.getTokenVersion();
        if (jwtTokenProvider.getTokenVersion(claims) != currentVersion) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "登录状态已失效");
        }
        return loginUser;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return bearer.substring(7).trim();
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                Result.error(ResultCode.UNAUTHORIZED.getCode(), message));
    }
}
