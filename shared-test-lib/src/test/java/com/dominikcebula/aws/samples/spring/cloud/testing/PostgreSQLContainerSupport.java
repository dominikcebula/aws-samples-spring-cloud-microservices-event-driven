package com.dominikcebula.aws.samples.spring.cloud.testing;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLContainerSupport {
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15.4");

    public static void start() {
        POSTGRESQL_CONTAINER.start();
    }

    public static void stop() {
        POSTGRESQL_CONTAINER.stop();
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }
}
