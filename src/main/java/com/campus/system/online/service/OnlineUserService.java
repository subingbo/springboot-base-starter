package com.campus.system.online.service;

import com.campus.auth.dto.OnlineSession;
import com.campus.auth.service.TokenSessionService;
import com.campus.common.page.PageResult;
import com.campus.security.jwt.JwtProperties;
import com.campus.system.online.dto.OnlineUserQuery;
import com.campus.system.online.dto.OnlineUserResponse;
import com.campus.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final TokenSessionService tokenSessionService;
    private final UserService userService;
    private final JwtProperties jwtProperties;

    public PageResult<OnlineUserResponse> page(OnlineUserQuery query) {
        String username = query.getUsername();
        String ip = query.getIp();
        List<OnlineUserResponse> filtered = tokenSessionService.listOnlineSessions().stream()
                .filter(session -> username == null || username.isBlank()
                        || (session.getUsername() != null && session.getUsername().contains(username)))
                .filter(session -> ip == null || ip.isBlank()
                        || (session.getIp() != null && session.getIp().contains(ip)))
                .map(this::toResponse)
                .toList();
        int pageNum = Math.max(1, query.getPageNum());
        int pageSize = Math.max(1, query.getPageSize());
        int total = filtered.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        int pages = (total + pageSize - 1) / pageSize;
        return new PageResult<>(total, pageNum, pageSize, pages, filtered.subList(from, to));
    }

    public void forceLogout(String tokenId) {
        OnlineSession session = tokenSessionService.getOnlineSession(tokenId);
        tokenSessionService.removeOnlineSession(tokenId);
        if (session != null) {
            tokenSessionService.revokeFamily(session.getFamilyId());
            tokenSessionService.blacklistToken(tokenId, jwtProperties.getAccessTokenExpiration());
        }
    }

    public void forceLogoutUser(Long userId) {
        userService.forceLogout(userId, true);
    }

    private OnlineUserResponse toResponse(OnlineSession session) {
        OnlineUserResponse response = new OnlineUserResponse();
        response.setTokenId(session.getTokenId());
        response.setUserId(session.getUserId());
        response.setUsername(session.getUsername());
        response.setNickname(session.getNickname());
        response.setIp(session.getIp());
        response.setBrowser(session.getBrowser());
        response.setOs(session.getOs());
        response.setLoginTime(session.getLoginTime());
        return response;
    }
}
