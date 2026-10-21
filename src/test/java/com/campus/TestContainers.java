package com.campus;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * 单例测试容器：整个 JVM 启动一次，避免多个测试类之间容器被停止导致连接超时。
 * Ryuk 在 JVM 退出时负责清理。
 */
public final class TestContainers {

    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("campus")
            .withUsername("campus")
            .withPassword("campus123")
            .withInitScript("db/test-init.sql");

    public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }

    private TestContainers() {
    }
}
