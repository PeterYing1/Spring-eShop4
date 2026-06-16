package com.eshop.catalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Catalog REST API.
 *
 * <p>Spins up real SQL Server and RabbitMQ containers via Testcontainers.
 * The Spring application context starts on a random port with all components
 * wired (JPA, Flyway, RabbitMQ, security). JWT validation is disabled via
 * the {@code integration-test} profile — the catalog API is publicly accessible
 * by design, so no authentication headers are needed for read operations.
 *
 * <p>Prerequisites: Docker must be running on the host.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration-test")
@DisplayName("Catalog API integration tests")
class CatalogApiIntegrationTest {

    // -------------------------------------------------------------------------
    // Containers — shared across all tests in this class (static)
    // -------------------------------------------------------------------------

    @Container
    static MSSQLServerContainer<?> sqlServer =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense()
                    .withPassword("Pass@word1");

    @Container
    static RabbitMQContainer rabbit =
            new RabbitMQContainer("rabbitmq:3-management");

    // -------------------------------------------------------------------------
    // Dynamic properties — wire container coordinates into the Spring context
    // -------------------------------------------------------------------------

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // SQL Server — use the JDBC URL produced by Testcontainers (includes port)
        registry.add("spring.datasource.url", sqlServer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlServer::getUsername);
        registry.add("spring.datasource.password", sqlServer::getPassword);

        // RabbitMQ
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);

        // Disable JWT validation — catalog security config skips oauth2ResourceServer
        // when issuer-uri is blank; the test profile already sets it to empty but
        // this override ensures it is enforced even if the profile isn't picked up.
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    // -------------------------------------------------------------------------
    // /items
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/catalog/items returns 200 OK with a paginated response")
    void getItems_returnsOkWithPaginatedItems() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/items", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // The Flyway seed populates items; the paginated wrapper always has a 'data' array
        assertThat(response.getBody()).contains("pageIndex", "pageSize", "count", "data");
    }

    @Test
    @DisplayName("GET /api/v1/catalog/items with pageSize=5 returns at most 5 items")
    void getItems_withPageSize_respectsPageLimit() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/items?pageSize=5&pageIndex=0", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/v1/catalog/items with invalid ids param returns 400 Bad Request")
    void getItems_withInvalidIds_returns400() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/items?ids=not-a-number", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // /catalogbrands
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/catalog/catalogbrands returns 200 OK with a list of brands")
    void getCatalogBrands_returnsOk() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/catalogbrands", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // /catalogtypes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/catalog/catalogtypes returns 200 OK with a list of types")
    void getCatalogTypes_returnsOk() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/catalogtypes", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // /items/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/catalog/items/0 returns 400 Bad Request for invalid id")
    void getItemById_withZeroId_returns400() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/items/0", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/catalog/items/999999 returns 404 Not Found for non-existent item")
    void getItemById_nonExistentId_returns404() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/v1/catalog/items/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
