package com.campus.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 缓存 JSON 请求体，使防重复提交切面能够把请求体摘要纳入幂等键。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CachedBodyFilter extends OncePerRequestFilter {

    public static final String WRAPPER_ATTRIBUTE = CachedBodyFilter.class.getName() + ".wrapper";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
            // 即使 RequestContextHolder 取到的是原始 request，也能通过属性拿到缓存包装器。
            request.setAttribute(WRAPPER_ATTRIBUTE, wrapper);
            filterChain.doFilter(wrapper, response);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
