package com.campus.common.aspect;

import com.campus.common.annotation.DataScope;
import com.campus.common.datascope.DataScopeAware;
import com.campus.common.datascope.DataScopeFilter;
import com.campus.common.datascope.DataScopeService;
import com.campus.config.SecurityProperties;
import com.campus.security.user.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(3)
@RequiredArgsConstructor
public class DataScopeAspect {

    private final DataScopeService dataScopeService;
    private final SecurityProperties securityProperties;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        if (!SecurityUtils.isAdmin(securityProperties.getAdminRoleCode())) {
            Long userId = SecurityUtils.getUserIdOrNull();
            if (userId != null) {
                DataScopeFilter filter = dataScopeService.buildFilter(userId);
                for (Object arg : joinPoint.getArgs()) {
                    if (arg instanceof DataScopeAware aware) {
                        aware.setDataScope(filter);
                        break;
                    }
                }
            }
        }
        return joinPoint.proceed();
    }
}
