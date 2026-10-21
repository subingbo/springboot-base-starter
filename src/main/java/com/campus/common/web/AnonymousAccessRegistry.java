package com.campus.common.web;

import com.campus.common.annotation.Anonymous;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 启动时扫描 @Anonymous 标注的接口，供 SecurityFilterChain 与 JWT Filter 复用。
 */
@Slf4j
@Component
public class AnonymousAccessRegistry implements ApplicationListener<ContextRefreshedEvent>, RequestMatcher {

    private final PathPatternParser pathPatternParser = new PathPatternParser();
    private volatile List<PathPattern> patterns = List.of();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            RequestMappingHandlerMapping mapping = event.getApplicationContext()
                    .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Set<String> paths = new LinkedHashSet<>();
            mapping.getHandlerMethods().forEach((info, handlerMethod) -> {
                boolean anonymous = AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), Anonymous.class)
                        || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), Anonymous.class);
                if (anonymous) {
                    paths.addAll(info.getPatternValues());
                }
            });
            this.patterns = paths.stream().map(pathPatternParser::parse).toList();
            if (!paths.isEmpty()) {
                log.info("anonymous access paths registered: {}", paths);
            }
        } catch (Exception ex) {
            log.warn("failed to scan anonymous access paths", ex);
        }
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (patterns.isEmpty()) {
            return false;
        }
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        PathContainer path = PathContainer.parsePath(uri);
        return patterns.stream().anyMatch(pattern -> pattern.matches(path));
    }
}
