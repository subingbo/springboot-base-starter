package com.campus.common.mybatis;

import com.campus.common.base.BaseEntity;
import com.campus.security.user.SecurityUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 自动填充 BaseEntity 的审计字段，避免每个 Service 重复设置。
 */
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class AuditFieldInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        SqlCommandType commandType = statement.getSqlCommandType();
        if (commandType == SqlCommandType.INSERT) {
            fill(parameter, true);
        } else if (commandType == SqlCommandType.UPDATE) {
            fill(parameter, false);
        }
        return invocation.proceed();
    }

    private void fill(Object parameter, boolean create) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visit(parameter, create, visited);
    }

    private void visit(Object parameter, boolean create, Set<Object> visited) {
        if (parameter == null) {
            return;
        }
        if (parameter instanceof BaseEntity entity) {
            if (!visited.add(entity)) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            Long userId = SecurityUtils.getUserIdOrNull();
            if (create) {
                if (entity.getCreateTime() == null) {
                    entity.setCreateTime(now);
                }
                if (entity.getCreateBy() == null) {
                    entity.setCreateBy(userId);
                }
                if (entity.getDeleted() == null) {
                    entity.setDeleted(0);
                }
                if (entity.getVersion() == null) {
                    entity.setVersion(0);
                }
            }
            entity.setUpdateTime(now);
            if (userId != null) {
                entity.setUpdateBy(userId);
            }
            return;
        }
        if (parameter instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                visit(value, create, visited);
            }
            return;
        }
        if (parameter instanceof Collection<?> collection) {
            for (Object value : collection) {
                visit(value, create, visited);
            }
            return;
        }
        if (parameter.getClass().isArray()) {
            Object[] array = (Object[]) parameter;
            for (Object value : array) {
                visit(value, create, visited);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // no-op
    }
}
