package com.campus.config;

import com.campus.common.mybatis.AuditFieldInterceptor;
import com.campus.common.mybatis.SlowSqlInterceptor;
import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MybatisConfig {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "false");
        properties.setProperty("params", "count=countSql");
        interceptor.setProperties(properties);
        return interceptor;
    }

    @Bean
    public AuditFieldInterceptor auditFieldInterceptor() {
        return new AuditFieldInterceptor();
    }

    @Bean
    public SlowSqlInterceptor slowSqlInterceptor(
            @Value("${campus.log.slow-sql-ms:500}") long slowSqlMs) {
        return new SlowSqlInterceptor(slowSqlMs);
    }
}
