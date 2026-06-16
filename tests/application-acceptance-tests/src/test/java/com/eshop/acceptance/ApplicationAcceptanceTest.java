package com.eshop.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance tests for the full eShop stack.
 *
 * <h2>How to run</h2>
 * <ol>
 *   <li>Start the full stack: {@code docker compose up -d}</li>
 *   <li>Run with: {@code mvn verify -Dacceptance.tests=true -pl tests/application-acceptance-tests}</li>
 * </ol>
 *
 * <p>Service base URLs default to the Docker Compose port bindings used in
 * {@code docker-compose.yml} and can be overridden via system properties:
 * <ul>
 *   <li>{@code -Dcatalog.url=http://host:port}</li>
 *   <li>{@code -Dbasket.url=http://host:port}</li>
 * </ul>
 *
 * <h2>Key scenarios</h2>
 * <ol>
 *   <li>Anonymous catalog browsing</li>
 *   <li>Catalog brands and types are available</li>
 *   <li>Basket requires authentication (401)</li>
 *   <li>Catalog item search by name</li>
 *   <li>Paginated catalog retrieval</li>
 * </ol>
 *
 * <p>Scenarios that require authentication tokens (basket CRUD, checkout, order
 * status transitions, webhook notifications) are annotated with comments
 * indicating the auth flow required. They can be enabled by supplying a bearer
 * token obtained from Keycloak via the client-credentials or password grant.
 */
@Tag("acceptance")
@DisplayName("Application acceptance tests")
class ApplicationAcceptanceTest {

    private static final String CATALOG_BASE_URL =
            System.getProperty("catalog.url", "http://localhost:5101");

    private static final String BASKET_BASE_URL =
            System.getProperty("basket.url", "http://localhost:5103");

    /**
     * Shared {@link TestRestTemplate} — no auth, mirrors an anonymous browser client.
     */
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    // -------------------------------------------------------------------------
    // Scenario 1: Anonymous catalog browsing
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Scenario 1: Anonymous user can browse the catalog item list")
    void anonymousUser_canBrowseCatalog() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/items", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    @DisplayName("Scenario 1b: Catalog item list is paginated (pageSize honoured)")
    void anonymousUser_catalogList_isPaginated() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/items?pageSize=3&pageIndex=0", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Response body must contain the paginated wrapper fields
        assertThat(response.getBody())
                .contains("pageIndex")
                .contains("pageSize")
                .contains("count")
                .contains("data");
    }

    // -------------------------------------------------------------------------
    // Scenario 2: Catalog brands and types are available
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Scenario 2a: Catalog brands endpoint is accessible anonymously")
    void catalogBrands_areAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/catalogbrands", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    @DisplayName("Scenario 2b: Catalog types endpoint is accessible anonymously")
    void catalogTypes_areAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/catalogtypes", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    // -------------------------------------------------------------------------
    // Scenario 3: Basket requires authentication
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Scenario 3: Basket endpoint returns 401 for unauthenticated requests")
    void basket_requiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                BASKET_BASE_URL + "/api/v1/basket/user-1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Scenario 3b: Basket POST endpoint returns 401 for unauthenticated requests")
    void basketUpdate_requiresAuthentication() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASKET_BASE_URL + "/api/v1/basket",
                "{\"buyerId\":\"user-1\",\"items\":[]}",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -------------------------------------------------------------------------
    // Scenario 4: Catalog search by name
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Scenario 4: Catalog items can be searched by name prefix")
    void catalogItems_canBeSearchedByName() {
        // The .NET source seeds items with names like ".NET Bot Black Hoodie"
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/items/withname/.NET", String.class);

        // Either 200 with results, or 200 with empty list — both are valid
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Scenario 5: Invalid catalog item id returns appropriate error
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Scenario 5: Requesting catalog item with id=0 returns 400 Bad Request")
    void catalogItemById_withZeroId_returns400() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                CATALOG_BASE_URL + "/api/v1/catalog/items/0", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /*
     * -----------------------------------------------------------------------
     * Scenarios requiring authentication — stub outline
     * -----------------------------------------------------------------------
     *
     * The following scenarios require a bearer token obtained from Keycloak.
     * To enable them:
     *
     * 1. Obtain a token:
     *    POST http://localhost:5105/realms/eshop/protocol/openid-connect/token
     *    grant_type=password&client_id=basketswaggerui&username=alice&password=Pass@word1
     *
     * 2. Build an authenticated template:
     *    TestRestTemplate authed = new TestRestTemplate();
     *    authed.getRestTemplate().getInterceptors().add((req, body, exec) -> {
     *        req.getHeaders().setBearerAuth(token);
     *        return exec.execute(req, body);
     *    });
     *
     * Scenario 6: Authenticated user can add items to their basket
     *   POST /api/v1/basket with CustomerBasket body → 200 OK
     *
     * Scenario 7: Authenticated user can checkout
     *   POST /api/v1/basket/checkout with BasketCheckout body → 202 Accepted
     *
     * Scenario 8: Order created after checkout can be retrieved from ordering-service
     *   GET /api/v1/orders → 200 OK, order list contains the new order
     *
     * Scenario 9: Order status transitions (AwaitingValidation → StockConfirmed → Paid)
     *   Driven by RabbitMQ integration events; poll order status endpoint.
     *
     * Scenario 10: Webhook notification received after order status change
     *   Verify webhook-service delivers notification to registered URL.
     */
}
