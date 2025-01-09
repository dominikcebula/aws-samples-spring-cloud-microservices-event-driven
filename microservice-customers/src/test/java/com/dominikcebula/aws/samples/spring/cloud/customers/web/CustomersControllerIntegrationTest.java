package com.dominikcebula.aws.samples.spring.cloud.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class CustomersControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void shouldReturnEmptyCustomersList() {
    }
}
