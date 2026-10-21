package com.campus.common.aspect;

import com.campus.common.annotation.OperLog;
import com.campus.common.util.IpUtils;
import com.campus.common.util.SensitiveDataMasker;
import com.campus.config.SecurityProperties;
import com.campus.security.user.LoginUser;
import com.campus.security.user.SecurityUtils;
import com.campus.system.log.entity.OperationLog;
import com.campus.system.log.service.OperLogService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
@Order(4)
@RequiredArgsConstructor
public class LogAspect {

    private final OperLogService operLogService;
    private final SecurityProperties securityProperties;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLog record = new OperationLog();
        record.setTitle(operLog.title());
        record.setBusinessType(operLog.businessType().name());
        record.setMethod(joinPoint.getSignature().toShortString());
        record.setOperTime(LocalDateTime.now());
        fillRequest(record);
        fillOperator(record);
        if (operLog.saveParam()) {
            record.setOperParam(SensitiveDataMasker.mask(buildArgs(joinPoint.getArgs()), 2000));
        }

        try {
            Object result = joinPoint.proceed();
            record.setSuccess(1);
            if (operLog.saveResult()) {
                record.setJsonResult(SensitiveDataMasker.mask(result, 4000));
            }
            return result;
        } catch (Throwable ex) {
            record.setSuccess(0);
            record.setErrorMsg(SensitiveDataMasker.truncate(ex.getMessage(), 2000));
            throw ex;
        } finally {
            record.setCostTime(System.currentTimeMillis() - start);
            try {
                operLogService.recordAsync(record);
            } catch (Exception ex) {
                log.error("failed to submit operation log", ex);
            }
        }
    }

    private void fillRequest(OperationLog record) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        record.setOperUrl(request.getRequestURI());
        record.setRequestMethod(request.getMethod());
        record.setOperIp(IpUtils.getClientIp(request, securityProperties.isTrustProxyHeaders()));
    }

    private void fillOperator(OperationLog record) {
        LoginUser loginUser = SecurityUtils.getLoginUserOrNull();
        if (loginUser != null) {
            record.setOperUserId(loginUser.getId());
            record.setOperName(loginUser.getUsername());
        }
    }

    private List<Object> buildArgs(Object[] args) {
        List<Object> values = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof ServletRequest || arg instanceof ServletResponse
                    || arg instanceof BindingResult || arg instanceof Principal) {
                continue;
            }
            if (arg instanceof MultipartFile file) {
                Map<String, Object> fileInfo = new LinkedHashMap<>();
                fileInfo.put("filename", file.getOriginalFilename());
                fileInfo.put("size", file.getSize());
                values.add(fileInfo);
                continue;
            }
            values.add(arg);
        }
        return values;
    }
}
