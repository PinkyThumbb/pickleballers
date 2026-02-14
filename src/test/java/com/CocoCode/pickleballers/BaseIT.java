package com.CocoCode.pickleballers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIT extends AbstractTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    public void beforeBaseIT() {
        objectMapper = new ObjectMapper();
        RestAssured.port = this.port;
    }

    // Start a Postgres container for all ITs
    private static final PostgreSQLContainer<?> postgres;

    static {
        // Docker config For Windows
        System.setProperty("DOCKER_HOST", "tcp://localhost:2375");
        System.setProperty("DOCKER_TLS_VERIFY", "0");

        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("pickleball")
                .withUsername("pickle")
                .withPassword("ball")
                .withStartupTimeout(Duration.ofMinutes(2));

        postgres.start();
    }

    // Dynamically inject container properties into Spring Boot
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
