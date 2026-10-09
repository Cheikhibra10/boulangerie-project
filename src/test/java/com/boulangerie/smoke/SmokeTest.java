package com.boulangerie.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(SmokeTest.SmokeTestConfig.class)
class SmokeTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("boulangerie_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        // ===== Database (Testcontainers) =====
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // ===== All Keycloak properties required by your code =====
        registry.add("keycloak.server-url", () -> "http://localhost:9095");
        registry.add("keycloak.realm", () -> "boulangerie-realm");
        registry.add("keycloak.client-id", () -> "boulangerie-api");
        registry.add("keycloak.client-secret", () -> "dummy-secret");
        registry.add("keycloak.admin.username", () -> "admin");
        registry.add("keycloak.admin.password", () -> "admin");
        registry.add("keycloak.admin.client-id", () -> "admin-cli");
        registry.add("keycloak.admin.client-secret", () -> "dummy-admin-secret");

        // Prevent real OAuth2 auto-config from trying to contact Keycloak
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration");
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void applicationContextLoads() {
        // If we reach here → the Spring context started successfully
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);

        // Accept 200 (public) or 401 (protected but the app is alive)
        assertThat(response.getStatusCode().value())
                .as("Health endpoint should be reachable (200 or 401)")
                .isIn(200, 401);
    }

    /**
     * Mock JwtDecoder so that WebSocket security (StompAuthChannelInterceptor)
     * can start even when real OAuth2 Resource Server is disabled.
     */
    @TestConfiguration
    static class SmokeTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "smoke-test-user")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }
}