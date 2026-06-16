# Spring Boot Conversion Design

**Source:** `C:\Projects\eShopOnContainers` — .NET 7 / ASP.NET Core microservice application (main branch)
**Target:** `C:\Projects\Spring-eShop4` — Java 21 / Spring Boot 3.x microservice application

This document is the primary implementation reference for converting every service, web application, API gateway, shared library, and infrastructure component from .NET to Java Spring Boot. It preserves the same features, architecture, public APIs, UI behavior, data model, and database systems as the source application.

---

## Table of Contents

1. [Source Application Summary](#1-source-application-summary)
2. [Conversion Goals and Constraints](#2-conversion-goals-and-constraints)
3. [Target Technology Stack](#3-target-technology-stack)
4. [Maven Multi-Module Project Structure](#4-maven-multi-module-project-structure)
5. [Infrastructure Services](#5-infrastructure-services)
6. [Common / Shared Libraries](#6-common--shared-libraries)
7. [Service Designs](#7-service-designs)
   - [7.1 Catalog Service](#71-catalog-service)
   - [7.2 Basket Service](#72-basket-service)
   - [7.3 Identity Service](#73-identity-service)
   - [7.4 Ordering Service](#74-ordering-service)
   - [7.5 Ordering Background Service](#75-ordering-background-service)
   - [7.6 Ordering Notification Service (SignalR → STOMP)](#76-ordering-notification-service-signalr--stomp)
   - [7.7 Payment Service](#77-payment-service)
   - [7.8 Marketing Service](#78-marketing-service)
   - [7.9 Location Service](#79-location-service)
   - [7.10 Webhooks Service](#710-webhooks-service)
8. [API Gateway and BFF Aggregators](#8-api-gateway-and-bff-aggregators)
9. [Web Applications](#9-web-applications)
   - [9.1 WebMVC App](#91-webmvc-app)
   - [9.2 WebSPA Host](#92-webspa-host)
   - [9.3 WebStatus App](#93-webstatus-app)
   - [9.4 Webhook Client App](#94-webhook-client-app)
10. [Data Model](#10-data-model)
11. [Public API Contracts](#11-public-api-contracts)
12. [Event Bus and Integration Events](#12-event-bus-and-integration-events)
13. [Authentication and Security](#13-authentication-and-security)
14. [Database Schemas and Migrations](#14-database-schemas-and-migrations)
15. [Docker Compose](#15-docker-compose)
16. [Testing Strategy](#16-testing-strategy)
17. [Framework Mapping Reference](#17-framework-mapping-reference)
18. [Implementation Order](#18-implementation-order)

---

## 1. Source Application Summary

eShopOnContainers is a containerized .NET microservice reference e-commerce application. Key facts:

- **Backend services:** Catalog, Basket, Ordering (domain-driven), Payment, Marketing, Locations, Webhooks, Identity.
- **Supporting services:** Ordering BackgroundTasks, Ordering SignalR Hub.
- **API layer:** Two Envoy API gateways (web, mobile) and two .NET BFF aggregator services (web shopping, mobile shopping).
- **Client apps:** ASP.NET Core MVC web app, Angular SPA, Xamarin mobile (out of scope for Spring Boot), WebStatus health dashboard, Webhooks client.
- **Infrastructure:** SQL Server (Catalog, Ordering, Identity, Marketing, Webhooks), Redis (Basket), MongoDB (Locations, Marketing read model), RabbitMQ event bus.
- **Patterns:** DDD aggregates in Ordering, CQRS with MediatR, integration event outbox, gRPC internal calls, OIDC/OAuth2 via IdentityServer4.

---

## 2. Conversion Goals and Constraints

| Goal | Detail |
|------|--------|
| Same features | Every user-visible feature present in the .NET application must be present in the Spring Boot application. |
| Same architecture | Microservice topology, Envoy gateways, BFF aggregators, event-driven integration events, and DDD structure in Ordering are all preserved. |
| Same public APIs | Every REST route, request/response shape, and HTTP status code must match exactly as documented in Section 11. |
| Same UI | WebMVC pages and WebSPA SPA behavior must be reproduced. Razor views become Thymeleaf templates; Angular SPA is kept as-is. |
| Same data model | All SQL tables, columns, constraints, indexes, seed data, Redis structures, and MongoDB collections are preserved. |
| Same database systems | SQL Server, Redis, MongoDB. No database system changes. |
| Java 21 / Spring Boot 3.x | Use the Spring Boot 3.x generation (Spring Framework 6.x, Jakarta EE 10). |
| Maven multi-module | Single parent `pom.xml` in the repo root with child modules per service and library. |
| Docker Compose | `docker-compose.yml` in the repo root reproduces the full stack locally. |

**Out of scope:** Xamarin mobile app (iOS/Droid/Windows), Azure DevOps CI/CD pipelines (recreate as needed separately), Azure Service Bus support (RabbitMQ is sufficient for local parity).

---

## 3. Target Technology Stack

| Layer | .NET Technology | Spring Boot Equivalent |
|-------|----------------|------------------------|
| REST controllers | ASP.NET Core `[ApiController]` | `@RestController` with `@RequestMapping` |
| MVC web app | ASP.NET Core MVC + Razor | Spring MVC + Thymeleaf |
| ORM | Entity Framework Core | Spring Data JPA + Hibernate 6 |
| SQL migrations | EF Core migrations | Flyway (preferred; or Liquibase) |
| Redis | StackExchange.Redis | Spring Data Redis (`spring-boot-starter-data-redis`) |
| MongoDB | MongoDB.Driver | Spring Data MongoDB (`spring-boot-starter-data-mongodb`) |
| Event bus | RabbitMQ.Client wrapped in BuildingBlocks | Spring AMQP (`spring-boot-starter-amqp`) |
| gRPC (internal) | `Grpc.AspNetCore` | `grpc-spring-boot-starter` (LogNet) or `net.devh:grpc-spring-boot-starter` |
| OIDC authority | IdentityServer4 | Keycloak (preferred) or Spring Authorization Server |
| OAuth2 resource server | `Microsoft.AspNetCore.Authentication.JwtBearer` | `spring-boot-starter-oauth2-resource-server` |
| OAuth2 client (MVC) | `Microsoft.AspNetCore.Authentication.OpenIdConnect` | `spring-boot-starter-oauth2-client` |
| MediatR | MediatR | Application service layer with Spring `@Service`; optional simple command bus abstraction |
| FluentValidation | FluentValidation | Jakarta Bean Validation (`jakarta.validation`) + custom validators |
| Swagger | Swashbuckle | `springdoc-openapi-starter-webmvc-ui` |
| Health checks | `Microsoft.Extensions.Diagnostics.HealthChecks` | Spring Boot Actuator (`/actuator/health`, mapped to `/hc` and `/liveness`) |
| SignalR | ASP.NET Core SignalR | Spring WebSocket + STOMP + SockJS |
| Background tasks | `IHostedService` + `BackgroundService` | `@Scheduled` + `ApplicationRunner` / `@EnableScheduling` |
| Polly retry | Polly | Resilience4j + Spring Retry |
| Logging | Serilog (structured) | SLF4J + Logback + Micrometer |
| API gateway | Envoy (keep as-is) | Envoy (unchanged) |
| Testing | xUnit + Moq + TestServer | JUnit 5 + Mockito + Spring Boot Test + MockMvc + Testcontainers |
| Build | .NET SDK / MSBuild | Maven 3.9+ |

---

## 4. Maven Multi-Module Project Structure

```
Spring-eShop4/                          ← repo root
  pom.xml                               ← parent POM (Spring Boot BOM, common deps, versions)
  docker-compose.yml
  docker-compose.override.yml
  SPRING_BOOT_CONVERSION_DESIGN.md
  common/
    pom.xml
    event-bus/                          ← common-event-bus module
      pom.xml
      src/main/java/com/eshop/eventbus/
    event-log/                          ← common-event-log module (outbox)
      pom.xml
      src/main/java/com/eshop/eventlog/
    web-support/                        ← common-web-support module
      pom.xml
      src/main/java/com/eshop/websupport/
    security/                           ← common-security module
      pom.xml
      src/main/java/com/eshop/security/
    test-support/                       ← common-test-support module
      pom.xml
      src/test/java/com/eshop/test/
  services/
    pom.xml
    catalog-service/
      pom.xml
      src/main/java/com/eshop/catalog/
      src/main/resources/
      src/test/java/com/eshop/catalog/
      Dockerfile
    basket-service/
      pom.xml
      src/main/java/com/eshop/basket/
      src/main/resources/
      src/test/java/com/eshop/basket/
      Dockerfile
    identity-service/                   ← Keycloak realm export OR Spring Authorization Server
      pom.xml
      keycloak-realm/
      src/main/java/com/eshop/identity/ ← thin adapter if using Spring Auth Server
      Dockerfile
    ordering-service/
      pom.xml
      src/main/java/com/eshop/ordering/
        api/
        application/
          commands/
          queries/
          behaviors/
        domain/
          aggregatesmodel/
            order/
            buyer/
          events/
          exceptions/
          seedwork/
        infrastructure/
          repositories/
          entityconfigurations/
        integrationevents/
        grpc/
      src/main/resources/
      src/test/java/com/eshop/ordering/
      Dockerfile
    ordering-background-service/
      pom.xml
      src/main/java/com/eshop/orderingbackground/
      Dockerfile
    ordering-notification-service/
      pom.xml
      src/main/java/com/eshop/orderingnotification/
      Dockerfile
    payment-service/
      pom.xml
      src/main/java/com/eshop/payment/
      Dockerfile
    marketing-service/
      pom.xml
      src/main/java/com/eshop/marketing/
      Dockerfile
    location-service/
      pom.xml
      src/main/java/com/eshop/location/
      Dockerfile
    webhooks-service/
      pom.xml
      src/main/java/com/eshop/webhooks/
      Dockerfile
  gateways/
    pom.xml
    web-shopping-aggregator/
      pom.xml
      src/main/java/com/eshop/webshoppingagg/
      Dockerfile
    mobile-shopping-aggregator/
      pom.xml
      src/main/java/com/eshop/mobileshoppingagg/
      Dockerfile
    envoy/
      webshopping/     ← Envoy config (unchanged from .NET)
      webmarketing/
      mobileshopping/
      mobilemarketing/
  web/
    pom.xml
    webmvc-app/
      pom.xml
      src/main/java/com/eshop/webmvc/
      src/main/resources/templates/     ← Thymeleaf
      src/main/resources/static/
      Dockerfile
    webspa-host/
      pom.xml
      src/main/java/com/eshop/webspa/
      src/main/resources/static/        ← Angular build output
      Dockerfile
    webstatus-app/
      pom.xml
      src/main/java/com/eshop/webstatus/
      Dockerfile
    webhook-client-app/
      pom.xml
      src/main/java/com/eshop/webhookclient/
      src/main/resources/templates/
      Dockerfile
  tests/
    pom.xml
    application-acceptance-tests/
      pom.xml
      src/test/java/com/eshop/acceptance/
```

### Parent POM Key Sections

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.x</version>
</parent>

<properties>
  <java.version>21</java.version>
  <springdoc.version>2.x.x</springdoc.version>
  <grpc.version>1.x.x</grpc.version>
  <testcontainers.version>1.x.x</testcontainers.version>
</properties>
```

---

## 5. Infrastructure Services

Reproduce the same containers as the .NET Docker Compose stack.

| Container name | Image | Ports | Purpose |
|----------------|-------|-------|---------|
| `sqldata` | `mcr.microsoft.com/mssql/server:2022-latest` | 1433 | SQL Server for Catalog, Ordering, Identity, Marketing, Webhooks |
| `nosqldata` | `mongo:6` | 27017 | MongoDB for Locations, Marketing read model |
| `basketdata` | `redis:7-alpine` | 6379 | Redis for Basket service |
| `rabbitmq` | `rabbitmq:3-management` | 5672, 15672 | RabbitMQ event bus |
| `seq` | `datalust/seq:latest` | 5341, 5380 | Optional structured log server |

SQL Server environment variables:
```
ACCEPT_EULA=Y
MSSQL_SA_PASSWORD=Pass@word
```

---

## 6. Common / Shared Libraries

### 6.1 `common/event-bus`

Equivalent to `BuildingBlocks/EventBus/EventBus` + `EventBusRabbitMQ`.

**Interfaces to define:**

```java
// com.eshop.eventbus.IEventBus
public interface IEventBus {
    void publish(IntegrationEvent event);
    <T extends IntegrationEvent> void subscribe(Class<T> eventType, Class<? extends IIntegrationEventHandler<T>> handler);
    <T extends IntegrationEvent> void unsubscribe(Class<T> eventType, Class<? extends IIntegrationEventHandler<T>> handler);
}

// com.eshop.eventbus.IIntegrationEventHandler
public interface IIntegrationEventHandler<T extends IntegrationEvent> {
    void handle(T event) throws Exception;
}

// com.eshop.eventbus.IntegrationEvent
public abstract class IntegrationEvent {
    private final UUID id;
    private final Instant creationDate;
    // ...
}
```

**RabbitMQ Implementation:**

- Exchange name: `eshop_event_bus`, type `direct`.
- Routing key: simple class name of the event (e.g., `OrderStatusChangedToPaidIntegrationEvent`).
- Each service declares its own durable queue named by `SubscriptionClientName` (e.g., `Ordering`, `Catalog`, `Basket`).
- Bind the queue to the exchange for each event type the service subscribes to.
- Message body: JSON with PascalCase property names (use Jackson `PropertyNamingStrategies.UPPER_CAMEL_CASE` or `@JsonProperty` annotations on event classes).
- Acknowledgement: manual ack after handler completes; nack-and-requeue on transient failure; dead-letter after configured retries.
- Use `spring-boot-starter-amqp` with `RabbitTemplate` for publishing and `@RabbitListener` for consumption.

**Spring AMQP configuration example:**

```java
@Bean
public TopicExchange eshopEventBus() {
    return new DirectExchange("eshop_event_bus", true, false);
}
```

### 6.2 `common/event-log`

Equivalent to `BuildingBlocks/IntegrationEventLogEF`.

Outbox table schema (per service that uses it — Catalog and Ordering):

```sql
CREATE TABLE IntegrationEventLog (
    EventId       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    EventTypeName NVARCHAR(255) NOT NULL,
    State         INT NOT NULL,            -- 0=NotPublished,1=InProgress,2=Published,3=PublishedFailed
    TimesSent     INT NOT NULL DEFAULT 0,
    CreationTime  DATETIME2 NOT NULL,
    Content       NVARCHAR(MAX) NOT NULL,
    TransactionId NVARCHAR(255) NULL
);
```

**Java interface:**

```java
public interface IIntegrationEventLogService {
    List<IntegrationEventLogEntry> retrieveEventLogsPendingToPublish(UUID transactionId);
    void saveEvent(IntegrationEvent event, /* JPA transaction */ EntityManager em);
    void markEventAsInProgress(UUID eventId);
    void markEventAsPublished(UUID eventId);
    void markEventAsFailed(UUID eventId);
}
```

### 6.3 `common/web-support`

- `ApplicationStartupExtensions`: Flyway migration runner, seed data runner, retry template for DB availability.
- `GlobalExceptionHandler`: `@ControllerAdvice` producing RFC 7807 problem details on unhandled exceptions.
- `PaginatedItemsViewModel<T>`: generic paginated response wrapper matching `.NET` contract:
  ```java
  public record PaginatedItemsViewModel<T>(int pageIndex, int pageSize, long count, List<T> data) {}
  ```
- `HttpGlobalExceptionFilter` equivalent: exception handler writing `{ "messages": ["..."] }` for domain exceptions.

### 6.4 `common/security`

- `IIdentityService` + `IdentityService`: extracts user identity (sub claim) from `SecurityContextHolder`.
- Shared JWT validation configuration (issuer, audience, JWKS endpoint).

---

## 7. Service Designs

### 7.1 Catalog Service

**Source:** `src/Services/Catalog/Catalog.API`
**Module:** `services/catalog-service`
**Artifact:** `catalog-service.jar`
**Port:** 5101 (HTTP), 9101 (gRPC)

#### 7.1.1 Package Structure

```
com.eshop.catalog
  api/
    CatalogController.java
    PicController.java
    HomeController.java
  application/
    CatalogSettings.java
  domain/
    CatalogItem.java
    CatalogBrand.java
    CatalogType.java
  infrastructure/
    CatalogContext.java          ← @Configuration for JPA
    CatalogRepository.java
    CatalogBrandRepository.java
    CatalogTypeRepository.java
    CatalogContextSeed.java
  integrationevents/
    ICatalogIntegrationEventService.java
    CatalogIntegrationEventService.java
    events/
      ProductPriceChangedIntegrationEvent.java
      OrderStockConfirmedIntegrationEvent.java
      OrderStockRejectedIntegrationEvent.java
    handlers/
      OrderStatusChangedToAwaitingValidationIntegrationEventHandler.java
      OrderStatusChangedToPaidIntegrationEventHandler.java
  grpc/
    CatalogGrpcService.java
  config/
    CatalogConfig.java
    SwaggerConfig.java
    SecurityConfig.java
```

#### 7.1.2 JPA Entities

**`CatalogItem`** → table `Catalog`

```java
@Entity
@Table(name = "Catalog")
public class CatalogItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "PictureFileName", length = 255)
    private String pictureFileName;

    @Transient
    private String pictureUri;   // built at read time from PicBaseUrl + PictureFileName

    @Column(name = "CatalogTypeId", nullable = false)
    private Integer catalogTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogTypeId", insertable = false, updatable = false)
    private CatalogType catalogType;

    @Column(name = "CatalogBrandId", nullable = false)
    private Integer catalogBrandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogBrandId", insertable = false, updatable = false)
    private CatalogBrand catalogBrand;

    @Column(name = "AvailableStock", nullable = false)
    private int availableStock;

    @Column(name = "RestockThreshold", nullable = false)
    private int restockThreshold;

    @Column(name = "MaxStockThreshold", nullable = false)
    private int maxStockThreshold;

    @Column(name = "OnReorder", nullable = false)
    private boolean onReorder;

    public int removeStock(int quantityDesired) {
        if (availableStock == 0) throw new CatalogDomainException("Empty stock, product item " + name + " is sold out");
        if (quantityDesired <= 0) throw new CatalogDomainException("Item units desired should be greater than zero");
        int removed = Math.min(quantityDesired, availableStock);
        availableStock -= removed;
        return removed;
    }

    public int addStock(int quantity) {
        int original = availableStock;
        if ((availableStock + quantity) > maxStockThreshold) {
            availableStock += (maxStockThreshold - availableStock);
        } else {
            availableStock += quantity;
        }
        onReorder = false;
        return availableStock - original;
    }
}
```

**`CatalogBrand`** → table `CatalogBrand`

```java
@Entity @Table(name = "CatalogBrand")
public class CatalogBrand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "Brand", nullable = false, length = 100)
    private String brand;
}
```

**`CatalogType`** → table `CatalogType`

```java
@Entity @Table(name = "CatalogType")
public class CatalogType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "Type", nullable = false, length = 100)
    private String type;
}
```

#### 7.1.3 Spring Data Repositories

```java
public interface CatalogItemRepository extends JpaRepository<CatalogItem, Integer> {
    Page<CatalogItem> findAllByOrderByName(Pageable pageable);
    Page<CatalogItem> findByNameStartingWith(String name, Pageable pageable);
    Page<CatalogItem> findByCatalogTypeId(int typeId, Pageable pageable);
    Page<CatalogItem> findByCatalogBrandId(int brandId, Pageable pageable);
    Page<CatalogItem> findByCatalogTypeIdAndCatalogBrandId(int typeId, int brandId, Pageable pageable);
    List<CatalogItem> findByIdIn(List<Integer> ids);
}
```

#### 7.1.4 REST Controller

```java
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
    @GetMapping("/items")
    public ResponseEntity<?> items(
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "0") int pageIndex,
        @RequestParam(required = false) String ids) { ... }

    @GetMapping("/items/{id}")
    public ResponseEntity<CatalogItem> itemById(@PathVariable int id) { ... }

    @GetMapping("/items/withname/{name}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsWithName(
        @PathVariable @NotEmpty String name,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "0") int pageIndex) { ... }

    @GetMapping("/items/type/{catalogTypeId}/brand/{catalogBrandId}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsByTypeAndBrand(
        @PathVariable int catalogTypeId,
        @PathVariable(required = false) Integer catalogBrandId,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "0") int pageIndex) { ... }

    @GetMapping("/items/type/all/brand/{catalogBrandId}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsByBrand(
        @PathVariable(required = false) Integer catalogBrandId,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "0") int pageIndex) { ... }

    @GetMapping("/catalogtypes")
    public List<CatalogType> catalogTypes() { ... }

    @GetMapping("/catalogbrands")
    public List<CatalogBrand> catalogBrands() { ... }

    @PutMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> updateItem(@RequestBody CatalogItem item) { ... }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createItem(@RequestBody CatalogItem item) { ... }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteItem(@PathVariable int id) { ... }
}

@RestController
@RequestMapping("/api/v1/catalog/items")
public class PicController {
    @GetMapping("/{catalogItemId}/pic")
    public ResponseEntity<Resource> getPic(@PathVariable int catalogItemId) { ... }
}
```

#### 7.1.5 Integration Events Published by Catalog

| Event class | Routing key | Trigger |
|-------------|-------------|---------|
| `ProductPriceChangedIntegrationEvent` | `ProductPriceChangedIntegrationEvent` | `PUT /api/v1/catalog/items` when price changes |
| `OrderStockConfirmedIntegrationEvent` | `OrderStockConfirmedIntegrationEvent` | Handler for `OrderStatusChangedToAwaitingValidationIntegrationEvent` — all items in stock |
| `OrderStockRejectedIntegrationEvent` | `OrderStockRejectedIntegrationEvent` | Handler for `OrderStatusChangedToAwaitingValidationIntegrationEvent` — stock insufficient |

#### 7.1.6 Integration Events Consumed by Catalog

| Event class | Source |
|-------------|--------|
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | Ordering |
| `OrderStatusChangedToPaidIntegrationEvent` | Ordering |

**Catalog uses the integration event outbox** — save event and catalog DB change in same transaction.

#### 7.1.7 gRPC

Implement `CatalogGrpcService` matching `Proto/catalog.proto`. The aggregators call this internally.

```proto
// From src/Services/Catalog/Catalog.API/Proto/catalog.proto
service Catalog {
  rpc GetItemById (CatalogItemRequest) returns (CatalogItemResponse);
  rpc GetItemsByIds (CatalogItemsRequest) returns (CatalogItemsResponse);
}
```

#### 7.1.8 Configuration (`application.yml`)

```yaml
server:
  port: 5101
spring:
  datasource:
    url: jdbc:sqlserver://sqldata:1433;databaseName=CatalogDb
    username: sa
    password: Pass@word
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
catalog:
  pic-base-url: http://localhost:5101/api/v1/catalog/items/[0]/pic/
  azure-storage-enabled: false
management:
  endpoints:
    web:
      exposure:
        include: health,info
  health:
    probes:
      enabled: true
```

#### 7.1.9 Seed Data

On first startup run the following seed via `ApplicationRunner`:

- `CatalogBrand`: Azure, .NET, Visual Studio, SQL Server, Other
- `CatalogType`: Mug, T-Shirt, Sheet, USB Memory Stick
- `CatalogItem`: 12 items referencing the above brands and types (see `Setup/CatalogItems.csv` in source)

---

### 7.2 Basket Service

**Source:** `src/Services/Basket/Basket.API`
**Module:** `services/basket-service`
**Port:** 5103 (HTTP), 9103 (gRPC)

#### 7.2.1 Package Structure

```
com.eshop.basket
  api/
    BasketController.java
  domain/
    CustomerBasket.java
    BasketItem.java
    BasketCheckout.java
    IBasketRepository.java
  infrastructure/
    RedisBasketRepository.java
  services/
    IIdentityService.java
    IdentityService.java
  integrationevents/
    events/
      UserCheckoutAcceptedIntegrationEvent.java
      ProductPriceChangedIntegrationEvent.java
      OrderStartedIntegrationEvent.java
    handlers/
      ProductPriceChangedIntegrationEventHandler.java
      OrderStartedIntegrationEventHandler.java
  grpc/
    BasketGrpcService.java
  config/
    BasketConfig.java
    SecurityConfig.java
```

#### 7.2.2 Redis Data Model

```java
@RedisHash   // NOT used — basket stored as plain JSON string
public class CustomerBasket {
    private String buyerId;
    private List<BasketItem> items = new ArrayList<>();
}

public class BasketItem {
    private String id;
    private int productId;
    private String productName;
    @JsonProperty private BigDecimal unitPrice;
    @JsonProperty private BigDecimal oldUnitPrice;
    @Min(1) private int quantity;
    private String pictureUrl;
}
```

**Repository implementation:**

```java
@Repository
public class RedisBasketRepository implements IBasketRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CustomerBasket getBasket(String customerId) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(customerId);
        return json == null ? null : objectMapper.readValue(json, CustomerBasket.class);
    }

    public CustomerBasket updateBasket(CustomerBasket basket) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(basket);
        redisTemplate.opsForValue().set(basket.getBuyerId(), json);
        return basket;
    }

    public void deleteBasket(String id) {
        redisTemplate.delete(id);
    }
}
```

Key: `basket.getBuyerId()`. Value: JSON string of `CustomerBasket`.

#### 7.2.3 REST Controller

```java
@RestController
@RequestMapping("/api/v1/basket")
@PreAuthorize("isAuthenticated()")
public class BasketController {
    @GetMapping("/{id}")
    public ResponseEntity<CustomerBasket> getById(@PathVariable String id) { ... }

    @PostMapping
    public ResponseEntity<CustomerBasket> update(@Valid @RequestBody CustomerBasket basket) { ... }

    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(
        @RequestBody BasketCheckout checkout,
        @RequestHeader(value = "x-requestid", required = false) String requestId) { ... }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) { ... }
}
```

**Checkout behavior:**
1. Get `userId` from `IdentityService.getUserIdentity()` (JWT sub claim).
2. If `requestId` header is a valid UUID, override `checkout.requestId`.
3. Load basket by `userId`; return `400` if not found.
4. Extract user name from JWT `name` claim.
5. Publish `UserCheckoutAcceptedIntegrationEvent` to RabbitMQ.
6. Return `202 Accepted`.

#### 7.2.4 Integration Events

Published:
- `UserCheckoutAcceptedIntegrationEvent` — during checkout

Consumed:
- `ProductPriceChangedIntegrationEvent` — update `BasketItem.unitPrice` and `oldUnitPrice` in Redis
- `OrderStartedIntegrationEvent` — delete basket for the buyer

#### 7.2.5 gRPC

Implement `BasketService` matching `Proto/basket.proto` for aggregator calls.

#### 7.2.6 Configuration

```yaml
server:
  port: 5103
spring:
  data:
    redis:
      host: basketdata
      port: 6379
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://identity-api:5105
management:
  endpoints:
    web:
      exposure:
        include: health
```

---

### 7.3 Identity Service

**Source:** `src/Services/Identity/Identity.API`
**Module:** `services/identity-service`
**Port:** 5105

#### 7.3.1 Approach: Keycloak (Preferred)

Replace IdentityServer4 with a Keycloak instance configured to match the same OIDC clients, scopes, and user model. The `identity-service` module is a Keycloak configuration module (not a Spring Boot app) containing:

- Keycloak realm export JSON: `keycloak-realm/eshop-realm.json`
- Docker configuration to start Keycloak with the realm imported

**Keycloak Docker image:**
```yaml
identity-api:
  image: quay.io/keycloak/keycloak:24
  command: start-dev --import-realm
  environment:
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin
    KC_DB: mssql
    KC_DB_URL: jdbc:sqlserver://sqldata:1433;databaseName=IdentityDb
    KC_DB_USERNAME: sa
    KC_DB_PASSWORD: Pass@word
  volumes:
    - ./services/identity-service/keycloak-realm:/opt/keycloak/data/import
  ports:
    - "5105:8080"
```

#### 7.3.2 Keycloak Realm Configuration

Create realm `eshop` with:

**Clients** (mirroring `Config.GetClients`):

| Client ID | Grant type | Redirect URIs | Scopes |
|-----------|------------|---------------|--------|
| `js` | implicit | `http://host:5104/` | openid, profile, orders, basket, locations, marketing, webshoppingagg, orders.signalrhub, webhooks |
| `mvc` | authorization_code + PKCE (Hybrid equivalent) | `http://host:5100/signin-oidc` | openid, profile, offline_access, orders, basket, locations, marketing, webshoppingagg, orders.signalrhub, webhooks |
| `webhooksclient` | authorization_code | `http://host:5114/signin-oidc` | openid, profile, offline_access, webhooks |
| `basketswaggerui` | implicit | `http://host:5103/swagger/oauth2-redirect.html` | basket |
| `orderingswaggerui` | implicit | `http://host:5102/swagger/oauth2-redirect.html` | orders |
| `webshoppingaggswaggerui` | implicit | `http://host:5202/swagger/oauth2-redirect.html` | webshoppingagg, basket |
| (and all other Swagger UI clients) | implicit | per-service swagger redirect | per-service scope |

**API Scopes:**
`orders`, `basket`, `marketing`, `locations`, `mobileshoppingagg`, `webshoppingagg`, `orders.signalrhub`, `webhooks`

**Demo user:** `demouser@microsoft.com` / `Pass@word1`

Custom user attributes mapped to JWT claims: `card_number`, `security_number`, `expiration`, `card_holder_name`, `card_type`, `street`, `city`, `state`, `country`, `zip_code`, `name`, `last_name`.

#### 7.3.3 Alternative: Spring Authorization Server

If Keycloak is not desired, implement `identity-service` as a Spring Boot application using `spring-authorization-server`. This is heavier to implement but avoids the Keycloak dependency.

Required endpoints:
- `GET /connect/authorize` — OIDC authorization
- `POST /connect/token` — token endpoint
- `GET /.well-known/openid-configuration` — discovery
- `GET /Account/Login`, `POST /Account/Login` — login form
- `POST /Account/Logout` — logout
- `GET /Account/Register`, `POST /Account/Register` — user registration
- `GET /Consent/Index`, `POST /Consent/Index` — consent screen

User store: Spring Data JPA on SQL Server (`AspNetUsers` table schema, Section 10 Identity Model).

---

### 7.4 Ordering Service

**Source:** `src/Services/Ordering/Ordering.API` + `Ordering.Domain` + `Ordering.Infrastructure`
**Module:** `services/ordering-service`
**Port:** 5102 (HTTP), 9102 (gRPC)

This is the richest service — implements DDD, CQRS, domain events, outbox, and idempotent commands.

#### 7.4.1 Package Structure

```
com.eshop.ordering
  api/
    OrdersController.java
    HomeController.java
  application/
    commands/
      CancelOrderCommand.java
      ShipOrderCommand.java
      CreateOrderCommand.java
      CreateOrderDraftCommand.java
      SetAwaitingValidationOrderStatusCommand.java
      SetStockConfirmedOrderStatusCommand.java
      SetPaidOrderStatusCommand.java
      SetShippedOrderStatusCommand.java
      SetCancelledOrderStatusCommand.java
      SetCancelledOrderStatusWhenStockIsRejectedCommand.java
    commands/handlers/
      CancelOrderCommandHandler.java
      ShipOrderCommandHandler.java
      CreateOrderCommandHandler.java
      CreateOrderDraftCommandHandler.java
      (one handler per command)
    behaviors/
      LoggingBehavior.java
      ValidatorBehavior.java
      TransactionBehavior.java
    queries/
      IOrderQueries.java
      OrderQueries.java              ← uses JdbcTemplate / jOOQ for read models
      OrderViewModel.java
      OrderSummary.java
      CardType.java
      OrderDraftDTO.java
    validators/
      CancelOrderCommandValidator.java
      CreateOrderCommandValidator.java
      ShipOrderCommandValidator.java
    idempotency/
      ClientRequest.java
      RequestManager.java
      IdentifiedCommand.java
      IdentifiedCommandHandler.java
  domain/
    aggregatesmodel/
      order/
        Order.java
        OrderItem.java
        OrderStatus.java
        Address.java               ← value object (embeddable)
        IOrderRepository.java
      buyer/
        Buyer.java
        PaymentMethod.java
        CardType.java
        IBuyerRepository.java
    events/
      OrderStartedDomainEvent.java
      BuyerAndPaymentMethodVerifiedDomainEvent.java
      OrderStatusChangedToAwaitingValidationDomainEvent.java
      OrderStatusChangedToStockConfirmedDomainEvent.java
      OrderStatusChangedToPaidDomainEvent.java
      OrderShippedDomainEvent.java
      OrderCancelledDomainEvent.java
    events/handlers/
      UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler.java
      ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler.java
      OrderStatusChangedToAwaitingValidationDomainEventHandler.java
      OrderStatusChangedToStockConfirmedDomainEventHandler.java
      OrderStatusChangedToPaidDomainEventHandler.java
    exceptions/
      OrderingDomainException.java
    seedwork/
      Entity.java
      IAggregateRoot.java
      IRepository.java
      IUnitOfWork.java
      ValueObject.java
      Enumeration.java
  infrastructure/
    OrderingContext.java
    repositories/
      OrderRepository.java
      BuyerRepository.java
    entityconfigurations/
      OrderEntityTypeConfiguration.java
      OrderItemEntityTypeConfiguration.java
      BuyerEntityTypeConfiguration.java
      PaymentMethodEntityTypeConfiguration.java
      CardTypeEntityTypeConfiguration.java
      OrderStatusEntityTypeConfiguration.java
      ClientRequestEntityTypeConfiguration.java
  integrationevents/
    events/
      UserCheckoutAcceptedIntegrationEvent.java
      GracePeriodConfirmedIntegrationEvent.java
      OrderPaymentSucceededIntegrationEvent.java
      OrderPaymentFailedIntegrationEvent.java
      (and all others — see Section 12)
    handlers/
      UserCheckoutAcceptedIntegrationEventHandler.java
      GracePeriodConfirmedIntegrationEventHandler.java
      OrderPaymentSucceededIntegrationEventHandler.java
      OrderPaymentFailedIntegrationEventHandler.java
      OrderStockConfirmedIntegrationEventHandler.java
      OrderStockRejectedIntegrationEventHandler.java
  grpc/
    OrderingGrpcService.java
```

#### 7.4.2 Domain Model

**`Order` aggregate root:**

```java
@Entity
@Table(name = "orders", schema = "ordering")
public class Order extends Entity implements IAggregateRoot {
    private Instant orderDate;

    @Embedded
    private Address address;            // owned value object, flattened columns

    @Column(name = "BuyerId")
    private Integer buyerId;

    @Column(name = "OrderStatusId", nullable = false)
    private int orderStatusId;

    @Column(name = "Description")
    private String description;

    @Column(name = "IsDraft")
    private boolean isDraft;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "OrderId")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "PaymentMethodId")
    private Integer paymentMethodId;

    // Domain methods
    public void addOrderItem(int productId, String productName, BigDecimal unitPrice,
                             BigDecimal discount, String pictureUrl, int units) { ... }
    public void setPaymentId(int id) { ... }
    public void setBuyerId(int id) { ... }
    public void setAwaitingValidationStatus() { ... }
    public void setStockConfirmedStatus() { ... }
    public void setPaidStatus() { ... }
    public void setShippedStatus() { ... }
    public void setCancelledStatus() { ... }
    public void setCancelledStatusWhenStockIsRejected(List<Integer> rejectedItems) { ... }
    public BigDecimal getTotal() { ... }
}
```

**`Address`** → embeddable value object:

```java
@Embeddable
public class Address {
    @Column(name = "Street") private String street;
    @Column(name = "City")   private String city;
    @Column(name = "State")  private String state;
    @Column(name = "Country") private String country;
    @Column(name = "ZipCode") private String zipCode;
}
```

**`OrderStatus`** → enum-like domain class:

```java
public class OrderStatus extends Enumeration {
    public static final OrderStatus SUBMITTED = new OrderStatus(1, "submitted");
    public static final OrderStatus AWAITING_VALIDATION = new OrderStatus(2, "awaitingvalidation");
    public static final OrderStatus STOCK_CONFIRMED = new OrderStatus(3, "stockconfirmed");
    public static final OrderStatus PAID = new OrderStatus(4, "paid");
    public static final OrderStatus SHIPPED = new OrderStatus(5, "shipped");
    public static final OrderStatus CANCELLED = new OrderStatus(6, "cancelled");
}
```

Persist `OrderStatus` as a lookup table `ordering.orderstatus`. Map via `@Column(name = "OrderStatusId")` integer FK.

**`OrderItem`** entity:

```java
@Entity
@Table(name = "orderItems", schema = "ordering")
public class OrderItem extends Entity {
    @Column(name = "ProductId")    private int productId;
    @Column(name = "ProductName")  private String productName;
    @Column(name = "UnitPrice")    private BigDecimal unitPrice;
    @Column(name = "Discount")     private BigDecimal discount;
    @Column(name = "Units")        private int units;
    @Column(name = "PictureUrl")   private String pictureUrl;
    @Column(name = "OrderId")      private int orderId;

    // constructor validates: units > 0, unitPrice*units >= discount
    // setNewDiscount rejects negative
    // addUnits rejects negative
}
```

**`Buyer`** aggregate:

```java
@Entity
@Table(name = "buyers", schema = "ordering")
public class Buyer extends Entity implements IAggregateRoot {
    @Column(name = "IdentityGuid", nullable = false, length = 200, unique = true)
    private String identityGuid;
    @Column(name = "Name", length = 255)
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "BuyerId")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    public PaymentMethod verifyOrAddPaymentMethod(int cardTypeId, String alias, String cardNumber,
        String securityNumber, String cardHolderName, Instant expiration, int orderId) { ... }
}
```

#### 7.4.3 Command Bus Pattern

Replace MediatR with a lightweight application layer:

```java
// Simple command bus
@Component
public class CommandBus {
    private final ApplicationContext context;
    public <R> R send(Object command) {
        var handlerType = resolveHandler(command.getClass());
        var handler = context.getBean(handlerType);
        return ((CommandHandler) handler).handle(command);
    }
}
```

Alternatively, use Spring's `ApplicationEventPublisher` for domain events and define explicit `@Service` handler classes per command.

**Transaction behavior** (equivalent to `TransactionBehaviour` MediatR pipeline):
- Wrap command handler execution in a transaction.
- On commit, retrieve all `NotPublished` outbox events for the transaction.
- Publish each event and mark as `Published`.

**Idempotency** (equivalent to `IdentifiedCommandHandler`):
- Check `ordering.requests` table for `requestId`.
- If found, return stored result.
- If not found, insert request record, execute command, store result.

#### 7.4.4 Query Layer

Use `JdbcTemplate` (not JPA) for read-side queries to return flat projections:

```java
@Repository
public class OrderQueries implements IOrderQueries {
    private final JdbcTemplate jdbc;

    public OrderViewModel getOrder(int orderId) {
        // Raw SQL joining orders, orderItems, orderstatus with lower-case property names in result
    }

    public List<OrderSummary> getOrdersFromUser(UUID userId) { ... }

    public List<CardType> getCardTypes() { ... }
}
```

Order query results use **lower-case property names** (`ordernumber`, `date`, `status`, `orderitems`, etc.) to match .NET contract exactly. Configure Jackson `@JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)` or explicit `@JsonProperty` annotations on query DTOs.

#### 7.4.5 REST Controller

```java
@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("isAuthenticated()")
public class OrdersController {
    @PutMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(
        @RequestBody CancelOrderCommand command,
        @RequestHeader("x-requestid") String requestId) { ... }

    @PutMapping("/ship")
    public ResponseEntity<Void> shipOrder(
        @RequestBody ShipOrderCommand command,
        @RequestHeader("x-requestid") String requestId) { ... }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderViewModel> getOrder(@PathVariable int orderId) { ... }

    @GetMapping
    public ResponseEntity<List<OrderSummary>> getOrders() { ... }

    @GetMapping("/cardtypes")
    public ResponseEntity<List<CardType>> getCardTypes() { ... }

    @PostMapping("/draft")
    public ResponseEntity<OrderDraftDTO> createOrderDraft(
        @RequestBody CreateOrderDraftCommand command) { ... }
}
```

#### 7.4.6 Integration Events

Published by Ordering (via outbox):

| Event | Trigger |
|-------|---------|
| `OrderStartedIntegrationEvent` | Order created from checkout |
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | Status → awaiting validation |
| `OrderStatusChangedToStockConfirmedIntegrationEvent` | Status → stock confirmed |
| `OrderStatusChangedToPaidIntegrationEvent` | Status → paid |
| `OrderStatusChangedToShippedIntegrationEvent` | Status → shipped |
| `OrderStatusChangedToCancelledIntegrationEvent` | Status → cancelled |

Consumed by Ordering:

| Event | Source |
|-------|--------|
| `UserCheckoutAcceptedIntegrationEvent` | Basket |
| `GracePeriodConfirmedIntegrationEvent` | Ordering BackgroundTasks |
| `OrderStockConfirmedIntegrationEvent` | Catalog |
| `OrderStockRejectedIntegrationEvent` | Catalog |
| `OrderPaymentSucceededIntegrationEvent` | Payment |
| `OrderPaymentFailedIntegrationEvent` | Payment |

---

### 7.5 Ordering Background Service

**Source:** `src/Services/Ordering/Ordering.BackgroundTasks`
**Module:** `services/ordering-background-service`
**Port:** 5111

Uses `@Scheduled` to periodically poll for orders in the grace period and publish `GracePeriodConfirmedIntegrationEvent`.

```java
@Component
@EnableScheduling
public class GracePeriodManagerService {
    @Scheduled(fixedDelayString = "${ordering.grace-period-ms:15000}")
    public void checkConfirmedGracePeriodOrders() {
        // Query orders in 'submitted' status older than grace period
        // Publish GracePeriodConfirmedIntegrationEvent for each
    }
}
```

Connects to the same `ordering` SQL Server schema as the ordering service. Shares `ordering-service` JPA entities or uses raw JDBC for the query.

---

### 7.6 Ordering Notification Service (SignalR → STOMP)

**Source:** `src/Services/Ordering/Ordering.SignalrHub`
**Module:** `services/ordering-notification-service`
**Port:** 5112

Replace ASP.NET Core SignalR with Spring WebSocket + STOMP + SockJS.

**WebSocket configuration:**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/hub/notificationhub")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

**Notification controller:**

```java
@Controller
public class OrderStatusNotificationHub {
    @Autowired private SimpMessagingTemplate messagingTemplate;

    public void sendOrderStatusUpdate(String userId, UpdatedOrderState state) {
        messagingTemplate.convertAndSendToUser(userId, "/topic/orders", state);
    }
}
```

**Integration event consumers:**

Subscribes to all `OrderStatusChanged*IntegrationEvent` events and pushes to connected clients:

```java
@RabbitListener(queues = "Ordering.signalrhub")
public void handleOrderStatusChanged(OrderStatusChangedToPaidIntegrationEvent event) {
    var state = new UpdatedOrderState(event.getOrderId(), "paid", event.getBuyerName(), event.getOrderDate());
    hub.sendOrderStatusUpdate(event.getBuyerId(), state);
}
```

**Client-visible payload** (`UpdatedOrderState`):

```json
{
  "orderId": 123,
  "status": "paid",
  "buyerName": "Jane Doe",
  "description": "The payment was performed..."
}
```

---

### 7.7 Payment Service

**Source:** `src/Services/Payment/Payment.API`
**Module:** `services/payment-service`
**Port:** 5108

Event-driven only — no REST API for external clients.

```java
@RabbitListener(queues = "Payment")
public class OrderStatusChangedToStockConfirmedIntegrationEventHandler
    implements IIntegrationEventHandler<OrderStatusChangedToStockConfirmedIntegrationEvent> {

    @Override
    public void handle(OrderStatusChangedToStockConfirmedIntegrationEvent event) {
        if (settings.isPaymentSucceeded()) {
            eventBus.publish(new OrderPaymentSucceededIntegrationEvent(event.getOrderId()));
        } else {
            eventBus.publish(new OrderPaymentFailedIntegrationEvent(event.getOrderId()));
        }
    }
}
```

Configuration property `payment.payment-succeeded=true` simulates success/failure.

---

### 7.8 Marketing Service

**Source:** `src/Services/Marketing/Marketing.API`
**Module:** `services/marketing-service`
**Port:** 5110

Dual storage: SQL Server (Campaign, Rule) + MongoDB (MarketingData user-location projection).

#### 7.8.1 JPA Entities (SQL Server)

**`Campaign`**:
```java
@Entity @Table(name = "Campaign")
public class Campaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String description;
    @Column(name = "\"From\"", nullable = false) private Instant from;  // quoted reserved word
    @Column(name = "\"To\"", nullable = false)   private Instant to;
    @Column(nullable = false) private String pictureUri;
    private String pictureName;
    private String detailsUri;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "CampaignId")
    private List<Rule> rules;
}
```

**`Rule`** — table-per-hierarchy with discriminator `RuleTypeId`:
```java
@Entity @Table(name = "Rule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "RuleTypeId")
public abstract class Rule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "CampaignId") private Integer campaignId;
    @Column(nullable = false) private String description;
}

@Entity @DiscriminatorValue("1") public class UserProfileRule extends Rule {}
@Entity @DiscriminatorValue("2") public class PurchaseHistoryRule extends Rule {}
@Entity @DiscriminatorValue("3") public class UserLocationRule extends Rule {
    @Column private Integer locationId;
}
```

#### 7.8.2 MongoDB Document

```java
@Document(collection = "MarketingReadDataModel")
public class MarketingData {
    @Id private String id;
    private String userId;
    private List<Location> locations;
    private Instant updateDate;

    @Data
    public static class Location {
        private int locationId;
        private String code;
        private String description;
    }
}
```

Repository: `MarketingDataRepository extends MongoRepository<MarketingData, String>`

#### 7.8.3 REST Controller

Implements all routes from `GET /api/v1/campaigns` through campaign location rule CRUD — see Section 11 for exact contracts.

#### 7.8.4 Integration Events

Consumed: `UserLocationUpdatedIntegrationEvent` — update MongoDB projection with new user location info.

---

### 7.9 Location Service

**Source:** `src/Services/Location/Locations.API`
**Module:** `services/location-service`
**Port:** 5109

MongoDB-only storage.

#### 7.9.1 MongoDB Documents

```java
@Document(collection = "Locations")
public class Location {
    @Id private String id;
    private int locationId;
    private String code;
    @Field("Parent_Id") private String parentId;
    private String description;
    private double latitude;
    private double longitude;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;       // spring-data-mongodb GeoJsonPoint
    private GeoJsonPolygon polygon;
}

@Document(collection = "UserLocation")
public class UserLocation {
    @Id private String id;
    private String userId;
    private int locationId;
    private Instant updateDate;
}
```

#### 7.9.2 REST Controller

```java
@RestController
@RequestMapping("/api/v1/locations")
@PreAuthorize("isAuthenticated()")
public class LocationsController {
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserLocation> getUserLocation(@PathVariable UUID userId) { ... }

    @GetMapping
    public ResponseEntity<List<Location>> getLocations() { ... }

    @GetMapping("/{locationId}")
    public ResponseEntity<Location> getLocation(@PathVariable String locationId) { ... }

    @PostMapping
    public ResponseEntity<Void> createOrUpdateUserLocation(
        @RequestBody UserLocationDto dto) { ... }
}
```

#### 7.9.3 Integration Events

Published: `UserLocationUpdatedIntegrationEvent` when user location changes.

---

### 7.10 Webhooks Service

**Source:** `src/Services/Webhooks/Webhooks.API`
**Module:** `services/webhooks-service`
**Port:** 5113

#### 7.10.1 JPA Entity

```java
@Entity @Table(name = "Subscriptions")
public class WebhookSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.ORDINAL) private WebhookType type;  // 1=CatalogItemPriceChange,2=OrderShipped,3=OrderPaid
    private Instant date;
    @Column(name = "DestUrl") private String destUrl;
    private String token;
    private String userId;
}
```

#### 7.10.2 REST Controller

Implements all `/api/v1/webhooks` routes from Section 11. Uses `WebClient` to validate grant URLs and to dispatch webhook payloads to subscribers.

#### 7.10.3 Integration Events Consumed

| Event | Webhook Type triggered |
|-------|----------------------|
| `ProductPriceChangedIntegrationEvent` | `CatalogItemPriceChange` |
| `OrderStatusChangedToShippedIntegrationEvent` | `OrderShipped` |
| `OrderStatusChangedToPaidIntegrationEvent` | `OrderPaid` |

---

## 8. API Gateway and BFF Aggregators

### 8.1 Envoy Gateways (Unchanged)

Copy the Envoy configuration from `src/ApiGateways/Envoy/config` without modification. Update upstream cluster hostnames to match Spring Boot service names.

Ports:
- Web Shopping Gateway: `5200` (HTTP)
- Web Marketing Gateway: `5201`
- Mobile Shopping Gateway: `5202`
- Mobile Marketing Gateway: `5203`

### 8.2 Web Shopping Aggregator

**Source:** `src/ApiGateways/Web.Bff.Shopping/aggregator`
**Module:** `gateways/web-shopping-aggregator`
**Port:** 5121 (HTTP)

```
com.eshop.webshoppingagg
  api/
    BasketController.java     ← POST/PUT /api/v1/basket, PUT /api/v1/basket/items, POST /api/v1/basket/items
    OrderController.java      ← GET /api/v1/order/draft/{basketId}
  clients/
    ICatalogClient.java       ← calls catalog-service (REST or gRPC)
    IBasketClient.java        ← calls basket-service (REST or gRPC)
    IOrderingClient.java      ← calls ordering-service (REST or gRPC)
  models/
    BasketData.java
    UpdateBasketRequest.java
    AddBasketItemRequest.java
    UpdateBasketItemsRequest.java
    OrderData.java
  config/
    ClientsConfig.java
    SecurityConfig.java
```

Uses `WebClient` or OpenFeign to call downstream services. For gRPC calls to Catalog, Basket, and Ordering, use the gRPC client stubs generated from proto files.

**Key behavior differences Web vs Mobile BFF:**
- `POST /api/v1/basket/items`: Web BFF increments existing line quantity; Mobile BFF always appends new line.

### 8.3 Mobile Shopping Aggregator

**Source:** `src/ApiGateways/Mobile.Bff.Shopping/aggregator`
**Module:** `gateways/mobile-shopping-aggregator`
**Port:** 5122 (HTTP)

Same routes as Web Shopping Aggregator but with the `POST /api/v1/basket/items` append-only behavior.

---

## 9. Web Applications

### 9.1 WebMVC App

**Source:** `src/Web/WebMVC`
**Module:** `web/webmvc-app`
**Port:** 5100

Replace Razor views with Thymeleaf templates. Replace ASP.NET Core OpenID Connect middleware with Spring Security OAuth2 client.

#### 9.1.1 Package Structure

```
com.eshop.webmvc
  controllers/
    CatalogController.java
    CartController.java
    OrderController.java
    OrderManagementController.java
    CampaignsController.java
    AccountController.java
  viewmodels/
    CatalogViewModel.java
    CartViewModel.java
    OrderViewModel.java
    CampaignViewModel.java
  services/
    ICatalogService.java       ← calls catalog via BFF
    IBasketService.java        ← calls basket via BFF
    IOrderingService.java      ← calls ordering via BFF
    ICampaignService.java      ← calls marketing via gateway
    ILocationService.java      ← calls location via gateway
  config/
    SecurityConfig.java
    WebClientConfig.java
  templates/                   ← Thymeleaf (in src/main/resources/templates)
    catalog/Index.html
    cart/Index.html
    order/Create.html
    order/Index.html
    order/Detail.html
    campaigns/Index.html
    campaigns/Details.html
    shared/layout.html
    account/Login.html
```

#### 9.1.2 Spring Security OAuth2 Client

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/Catalog/**").permitAll()
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/Account/SignIn")
                .defaultSuccessUrl("/Catalog/Index"))
            .logout(logout -> logout
                .logoutUrl("/Account/Signout")
                .oidcLogoutSuccessHandler(...));
        return http.build();
    }
}
```

#### 9.1.3 Route Mapping

| .NET Route | Spring Route | Notes |
|-----------|--------------|-------|
| `GET /Catalog/Index` | `GET /Catalog/Index` | Anonymous |
| `GET /Cart/Index` | `GET /Cart/Index` | Auth required |
| `POST /Cart/Index` | `POST /Cart/Index` | Anti-CSRF |
| `GET /Cart/AddToCart` | `GET /Cart/AddToCart` | Auth required |
| `GET /Order/Create` | `GET /Order/Create` | Auth required |
| `POST /Order/Checkout` | `POST /Order/Checkout` | Anti-CSRF |
| `GET /Order/Cancel` | `GET /Order/Cancel` | Auth required |
| `GET /Order/Detail` | `GET /Order/Detail` | Auth required |
| `GET /Order/Index` | `GET /Order/Index` | Auth required |
| `GET /OrderManagement/Index` | `GET /OrderManagement/Index` | Auth required |
| `POST /OrderManagement/OrderProcess` | `POST /OrderManagement/OrderProcess` | Anti-CSRF |
| `GET /Campaigns/Index` | `GET /Campaigns/Index` | Auth required |
| `GET /Campaigns/Details/{id}` | `GET /Campaigns/Details/{id}` | Auth required |
| `POST /Campaigns/CreateNewUserLocation` | `POST /Campaigns/CreateNewUserLocation` | Anti-CSRF |
| `GET /Account/SignIn` | `GET /Account/SignIn` | Redirect to OIDC |
| `GET /Account/Signout` | `GET /Account/Signout` | OIDC sign-out |

Default route: `/{controller=Catalog}/{action=Index}/{id?}`

Anti-forgery: Spring Security CSRF filter is enabled for all POST routes.

### 9.2 WebSPA Host

**Source:** `src/Web/WebSPA`
**Module:** `web/webspa-host`
**Port:** 5104

Keep the Angular SPA as-is. The Spring Boot host serves:
- Static Angular assets from `/resources/static/`
- `GET /Home/Configuration` → `AppSettings` JSON (gateway URLs, identity URL, etc.)
- `/hc`, `/liveness` actuator endpoints

```java
@RestController
public class HomeController {
    @GetMapping("/Home/Configuration")
    public AppSettings configuration() {
        return new AppSettings(
            purchaseUrl, marketingUrl, identityUrl,
            callbackUrl, signalrHubUrl, useCustomizationData, ...);
    }
}
```

### 9.3 WebStatus App

**Source:** `src/Web/WebStatus`
**Module:** `web/webstatus-app`
**Port:** 5107

Use Spring Boot Admin Client on each service + Spring Boot Admin Server in `webstatus-app`, OR implement a simple custom health dashboard.

Required routes:
- `GET /` → redirect to `/hc-ui`
- `GET /Config` → JSON config object listing all monitored service URLs
- `GET /Home/Error` → error view

Health checks to aggregate (same as .NET source):
- `webmvc`, `webspa`, `catalog-api`, `basket-api`, `ordering-api`, `identity-api`, `marketing-api`, `locations-api`, `payment-api`, `ordering-backgroundtasks`, `ordering-signalrhub`, `webhooks-api`, `webshoppingagg`, `mobileshoppingagg`

### 9.4 Webhook Client App

**Source:** `src/Web/WebhookClient`
**Module:** `web/webhook-client-app`
**Port:** 5114

Spring MVC + Thymeleaf + Spring Security OAuth2 Client.

Routes:
- `GET /` → display received webhooks list
- `GET /WebhooksList` → load and display user's subscriptions from Webhooks API
- `GET /RegisterWebhook` → registration form
- `POST /RegisterWebhook` → post subscription to Webhooks API; redirect to `/WebhooksList` on success
- `OPTIONS /check` → token validation endpoint for webhook grant URL
- `POST /webhook-received` → receive and store incoming webhook

---

## 10. Data Model

The following is the implementation target. The complete schema DDL is in `C:\Projects\Spring-eShopOnContainers\h2_schema.sql` and the seed data is in `h2_data.sql`. **Use SQL Server syntax for production migrations; adapt H2 schema for tests.**

### 10.1 Catalog Database (`CatalogDb`)

```sql
CREATE TABLE CatalogBrand (
    Id    INT IDENTITY PRIMARY KEY,
    Brand NVARCHAR(100) NOT NULL
);

CREATE TABLE CatalogType (
    Id   INT IDENTITY PRIMARY KEY,
    Type NVARCHAR(100) NOT NULL
);

CREATE TABLE Catalog (
    Id                INT IDENTITY PRIMARY KEY,
    Name              NVARCHAR(50) NOT NULL,
    Description       NVARCHAR(255) NULL,
    Price             DECIMAL(18,2) NOT NULL,
    PictureFileName   NVARCHAR(255) NULL,
    CatalogTypeId     INT NOT NULL REFERENCES CatalogType(Id) ON DELETE CASCADE,
    CatalogBrandId    INT NOT NULL REFERENCES CatalogBrand(Id) ON DELETE CASCADE,
    AvailableStock    INT NOT NULL DEFAULT 0,
    RestockThreshold  INT NOT NULL DEFAULT 0,
    MaxStockThreshold INT NOT NULL DEFAULT 0,
    OnReorder         BIT NOT NULL DEFAULT 0
);

CREATE INDEX IX_Catalog_CatalogBrandId ON Catalog(CatalogBrandId);
CREATE INDEX IX_Catalog_CatalogTypeId  ON Catalog(CatalogTypeId);

CREATE TABLE IntegrationEventLog (
    EventId       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    EventTypeName NVARCHAR(255) NOT NULL,
    State         INT NOT NULL,
    TimesSent     INT NOT NULL DEFAULT 0,
    CreationTime  DATETIME2 NOT NULL,
    Content       NVARCHAR(MAX) NOT NULL,
    TransactionId NVARCHAR(255) NULL
);
```

### 10.2 Basket (Redis)

No SQL tables. Redis key: `buyerId`. Value: JSON `CustomerBasket`.

### 10.3 Ordering Database (`OrderingDb`, schema `ordering`)

```sql
CREATE TABLE ordering.orderstatus (Id INT NOT NULL PRIMARY KEY, Name NVARCHAR(200) NOT NULL);
CREATE TABLE ordering.cardtypes   (Id INT NOT NULL PRIMARY KEY, Name NVARCHAR(200) NOT NULL);

CREATE TABLE ordering.buyers (
    Id           INT IDENTITY PRIMARY KEY,
    IdentityGuid NVARCHAR(200) NOT NULL,
    Name         NVARCHAR(255) NULL,
    CONSTRAINT UX_buyers_IdentityGuid UNIQUE (IdentityGuid)
);

CREATE TABLE ordering.paymentmethods (
    Id             INT IDENTITY PRIMARY KEY,
    Alias          NVARCHAR(200) NOT NULL,
    BuyerId        INT NOT NULL REFERENCES ordering.buyers(Id) ON DELETE CASCADE,
    CardHolderName NVARCHAR(200) NOT NULL,
    CardNumber     NVARCHAR(25) NOT NULL,
    CardTypeId     INT NOT NULL REFERENCES ordering.cardtypes(Id),
    Expiration     DATETIME2 NOT NULL
);

CREATE TABLE ordering.orders (
    Id             INT IDENTITY PRIMARY KEY,
    BuyerId        INT NULL REFERENCES ordering.buyers(Id),
    Description    NVARCHAR(255) NULL,
    OrderDate      DATETIME2 NOT NULL,
    OrderStatusId  INT NOT NULL REFERENCES ordering.orderstatus(Id),
    PaymentMethodId INT NULL REFERENCES ordering.paymentmethods(Id),
    Street         NVARCHAR(255) NULL,
    City           NVARCHAR(255) NULL,
    State          NVARCHAR(255) NULL,
    Country        NVARCHAR(255) NULL,
    ZipCode        NVARCHAR(255) NULL
);

CREATE TABLE ordering.orderItems (
    Id          INT IDENTITY PRIMARY KEY,
    Discount    DECIMAL(18,2) NOT NULL,
    OrderId     INT NOT NULL REFERENCES ordering.orders(Id) ON DELETE CASCADE,
    PictureUrl  NVARCHAR(255) NULL,
    ProductId   INT NOT NULL,
    ProductName NVARCHAR(255) NOT NULL,
    UnitPrice   DECIMAL(18,2) NOT NULL,
    Units       INT NOT NULL
);

CREATE TABLE ordering.requests (
    Id   UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    Name NVARCHAR(255) NOT NULL,
    Time DATETIME2 NOT NULL
);

CREATE TABLE IntegrationEventLog (  -- same as catalog
    EventId       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    EventTypeName NVARCHAR(255) NOT NULL,
    State         INT NOT NULL,
    TimesSent     INT NOT NULL DEFAULT 0,
    CreationTime  DATETIME2 NOT NULL,
    Content       NVARCHAR(MAX) NOT NULL,
    TransactionId NVARCHAR(255) NULL
);
```

Seed ordering lookup tables:

```sql
INSERT INTO ordering.orderstatus VALUES (1,'submitted'),(2,'awaitingvalidation'),
    (3,'stockconfirmed'),(4,'paid'),(5,'shipped'),(6,'cancelled');
INSERT INTO ordering.cardtypes VALUES (1,'Amex'),(2,'Visa'),(3,'MasterCard');
```

### 10.4 Identity Database (`IdentityDb`)

If using Spring Authorization Server, create tables for:
- `AspNetUsers` (with custom columns: `CardNumber`, `SecurityNumber`, `Expiration`, `CardHolderName`, `CardType`, `Street`, `City`, `State`, `Country`, `ZipCode`, `Name`, `LastName`)
- Standard Spring Security / Identity tables
- IdentityServer-equivalent tables for clients, scopes, grants

If using Keycloak, tables are managed by Keycloak and this schema is not needed.

### 10.5 Marketing Database (`MarketingDb`)

```sql
CREATE TABLE Campaign (
    Id          INT IDENTITY PRIMARY KEY,
    Name        NVARCHAR(255) NOT NULL,
    Description NVARCHAR(255) NOT NULL,
    [From]      DATETIME2 NOT NULL,
    [To]        DATETIME2 NOT NULL,
    PictureUri  NVARCHAR(255) NOT NULL,
    PictureName NVARCHAR(255) NULL,
    DetailsUri  NVARCHAR(255) NULL
);

CREATE TABLE Rule (
    Id         INT IDENTITY PRIMARY KEY,
    CampaignId INT NOT NULL REFERENCES Campaign(Id) ON DELETE CASCADE,
    Description NVARCHAR(255) NOT NULL,
    RuleTypeId INT NOT NULL,
    LocationId INT NULL
);
CREATE INDEX IX_Rule_CampaignId ON Rule(CampaignId);
```

MongoDB collections: `MarketingReadDataModel`, `Locations`, `UserLocation` (see Section 7.8 and 7.9).

### 10.6 Webhooks Database (`WebhooksDb`)

```sql
CREATE TABLE Subscriptions (
    Id      INT IDENTITY PRIMARY KEY,
    Type    INT NOT NULL,
    Date    DATETIME2 NOT NULL,
    DestUrl NVARCHAR(255) NULL,
    Token   NVARCHAR(255) NULL,
    UserId  NVARCHAR(255) NULL
);
```

---

## 11. Public API Contracts

All API contracts from the .NET source application must be preserved exactly. The complete contract specification is in `C:\Projects\Spring-eShopOnContainers\API_CONTRACTS.md`. Key requirements:

1. **URL paths must match exactly** (e.g., `/api/v1/catalog/items`, `/api/v1/basket/{id}`, `/api/v1/orders`).
2. **HTTP methods match** (GET, POST, PUT, DELETE).
3. **HTTP status codes match** (200, 201, 202, 204, 400, 404, 418).
4. **JSON property names match** — most are camelCase (Jackson default); Ordering query responses use all-lowercase (`ordernumber`, `date`, `status`, `orderitems`).
5. **`PaginatedItemsViewModel<T>`** shape: `{ pageIndex, pageSize, count, data }`.
6. **Request headers** — `x-requestid` for Basket checkout and Ordering cancel/ship.
7. **Health routes** — `GET /hc` and `GET /liveness` on every service.
8. **Swagger redirect** — `GET /` redirects to `~/swagger` on all API services.

**Jackson configuration for PascalCase integration event serialization:**

```java
@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Default is camelCase — correct for REST API responses
    }

    @Bean("integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)  // PascalCase for events
            .registerModule(new JavaTimeModule());
    }
}
```

---

## 12. Event Bus and Integration Events

Full specification in `C:\Projects\Spring-eShopOnContainers\EVENT_CONTRACTS.md`.

### 12.1 RabbitMQ Topology

- Exchange: `eshop_event_bus`, type `direct`, durable.
- Each consuming service has one durable queue named by `SubscriptionClientName`.
- Queue names: `Ordering`, `Catalog`, `Basket`, `Payment`, `Marketing`, `Webhooks`, `Ordering.signalrhub`.
- Routing key = full short class name of the event (e.g., `UserCheckoutAcceptedIntegrationEvent`).

### 12.2 Complete Event Inventory

| Event Class | Published by | Consumed by |
|-------------|-------------|-------------|
| `UserCheckoutAcceptedIntegrationEvent` | Basket | Ordering |
| `OrderStartedIntegrationEvent` | Ordering | Basket |
| `GracePeriodConfirmedIntegrationEvent` | Ordering.BackgroundTasks | Ordering |
| `OrderStatusChangedToAwaitingValidationIntegrationEvent` | Ordering | Catalog, SignalR Hub, Webhooks |
| `OrderStockConfirmedIntegrationEvent` | Catalog | Ordering |
| `OrderStockRejectedIntegrationEvent` | Catalog | Ordering |
| `OrderStatusChangedToStockConfirmedIntegrationEvent` | Ordering | Payment, SignalR Hub |
| `OrderPaymentSucceededIntegrationEvent` | Payment | Ordering |
| `OrderPaymentFailedIntegrationEvent` | Payment | Ordering |
| `OrderStatusChangedToPaidIntegrationEvent` | Ordering | Catalog, SignalR Hub, Webhooks |
| `OrderStatusChangedToShippedIntegrationEvent` | Ordering | SignalR Hub, Webhooks |
| `OrderStatusChangedToCancelledIntegrationEvent` | Ordering | SignalR Hub |
| `ProductPriceChangedIntegrationEvent` | Catalog | Basket, Webhooks |
| `UserLocationUpdatedIntegrationEvent` | Locations | Marketing |

### 12.3 Outbox Pattern

**Services using outbox:** Catalog, Ordering.

```java
@Transactional
public void saveEventAndContextChangesAsync(IntegrationEvent event, EntityManager em) {
    // 1. Save domain changes (already in transaction)
    // 2. Save IntegrationEventLog row (State=NotPublished) in same transaction
    // 3. After commit: mark InProgress, publish to RabbitMQ, mark Published
    //    On exception: mark PublishedFailed
}
```

**Services using direct publish (no outbox):** Basket, Payment, Locations, Ordering.BackgroundTasks.

### 12.4 Event Payload Shapes

Events use PascalCase JSON (Newtonsoft.Json default compatibility). Example:

```json
{
  "Id": "1d0f969f-0d77-4db1-830d-7c3f31e35a70",
  "CreationDate": "2026-06-13T12:00:00Z",
  "OrderId": 123,
  "OrderStatus": "paid",
  "BuyerName": "Jane Doe"
}
```

Configure Jackson on the event bus ObjectMapper with `PropertyNamingStrategies.UPPER_CAMEL_CASE`.

---

## 13. Authentication and Security

### 13.1 Resource Server (Backend APIs)

All backend API services (Catalog, Basket, Ordering, Marketing, Locations, Webhooks) configure Spring Security as an OAuth2 resource server:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)     // REST APIs use bearer tokens
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hc", "/liveness", "/swagger/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.issuerUri(issuerUri)));
        return http.build();
    }
}
```

Catalog API is unauthenticated (no `[Authorize]` on controller) — adjust `authorizeHttpRequests` to `permitAll()` for catalog.

### 13.2 OAuth2 Client (WebMVC, WebhookClient)

```java
spring:
  security:
    oauth2:
      client:
        registration:
          eshop:
            client-id: mvc
            client-secret: secret
            scope: openid,profile,orders,basket,locations,marketing,webshoppingagg,orders.signalrhub,webhooks
            redirect-uri: "{baseUrl}/signin-oidc"
            authorization-grant-type: authorization_code
        provider:
          eshop:
            issuer-uri: http://identity-api:5105
```

### 13.3 Token Claims

Extract from JWT:
- `sub` claim → user identity (used as `buyerId` and for order scoping)
- `name` claim → display name (used in checkout and basket events)
- `email` claim → user email

```java
@Service
public class IdentityService implements IIdentityService {
    public String getUserIdentity() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var jwt = (JwtAuthenticationToken) auth;
        return jwt.getToken().getClaimAsString("sub");
    }
}
```

---

## 14. Database Schemas and Migrations

### 14.1 Flyway

Each service that owns a SQL database includes Flyway migrations:

```
src/main/resources/db/migration/
  V1__Initial_Schema.sql
  V2__Seed_Data.sql
```

Flyway location per service:

| Service | Database | Flyway location |
|---------|----------|-----------------|
| catalog-service | CatalogDb | `classpath:db/migration/catalog` |
| ordering-service | OrderingDb | `classpath:db/migration/ordering` |
| identity-service | IdentityDb | (Keycloak manages its own) |
| marketing-service | MarketingDb | `classpath:db/migration/marketing` |
| webhooks-service | WebhooksDb | `classpath:db/migration/webhooks` |

### 14.2 Multiple Databases on One SQL Server

Locally, all services share the same SQL Server container but use separate logical databases. Configure per-service datasources pointing to different database names:

```yaml
# catalog-service
spring.datasource.url: jdbc:sqlserver://sqldata:1433;databaseName=CatalogDb;encrypt=false
# ordering-service
spring.datasource.url: jdbc:sqlserver://sqldata:1433;databaseName=OrderingDb;encrypt=false
```

### 14.3 H2 for Tests

For unit and integration tests, use H2 in-memory with compatibility mode:

```yaml
# test application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MSSQLServer;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

H2 schema is in `C:\Projects\Spring-eShopOnContainers\h2_schema.sql` and seed data in `h2_data.sql`.

---

## 15. Docker Compose

```yaml
# docker-compose.yml (root of Spring-eShop4 repo)
version: '3.9'

services:
  # Infrastructure
  sqldata:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      ACCEPT_EULA: "Y"
      MSSQL_SA_PASSWORD: Pass@word
    ports: ["1433:1433"]
    volumes: [sqldata:/var/opt/mssql]

  nosqldata:
    image: mongo:6
    ports: ["27017:27017"]
    volumes: [nosqldata:/data/db]

  basketdata:
    image: redis:7-alpine
    ports: ["6379:6379"]
    volumes: [basketdata:/data]

  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]

  seq:
    image: datalust/seq:latest
    environment:
      ACCEPT_EULA: "Y"
    ports: ["5341:5341", "5380:80"]

  # Identity
  identity-api:
    image: quay.io/keycloak/keycloak:24
    command: start-dev --import-realm
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    volumes:
      - ./services/identity-service/keycloak-realm:/opt/keycloak/data/import
    ports: ["5105:8080"]
    depends_on: [sqldata]

  # Backend services
  catalog-api:
    build: services/catalog-service
    ports: ["5101:5101", "9101:9101"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqldata:1433;databaseName=CatalogDb;encrypt=false
      SPRING_RABBITMQ_HOST: rabbitmq
      CATALOG_PIC_BASE_URL: http://host.docker.internal:5101/api/v1/catalog/items/[0]/pic/
    depends_on: [sqldata, rabbitmq]

  basket-api:
    build: services/basket-service
    ports: ["5103:5103", "9103:9103"]
    environment:
      SPRING_DATA_REDIS_HOST: basketdata
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [basketdata, rabbitmq, identity-api]

  ordering-api:
    build: services/ordering-service
    ports: ["5102:5102", "9102:9102"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqldata:1433;databaseName=OrderingDb;encrypt=false
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [sqldata, rabbitmq, identity-api]

  ordering-backgroundtasks:
    build: services/ordering-background-service
    ports: ["5111:5111"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqldata:1433;databaseName=OrderingDb;encrypt=false
      SPRING_RABBITMQ_HOST: rabbitmq
      ORDERING_GRACE_PERIOD_MS: 15000
    depends_on: [sqldata, rabbitmq]

  ordering-signalrhub:
    build: services/ordering-notification-service
    ports: ["5112:5112"]
    environment:
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [rabbitmq, identity-api]

  payment-api:
    build: services/payment-service
    ports: ["5108:5108"]
    environment:
      SPRING_RABBITMQ_HOST: rabbitmq
      PAYMENT_PAYMENT_SUCCEEDED: "true"
    depends_on: [rabbitmq]

  marketing-api:
    build: services/marketing-service
    ports: ["5110:5110"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqldata:1433;databaseName=MarketingDb;encrypt=false
      SPRING_DATA_MONGODB_URI: mongodb://nosqldata:27017/MarketingDb
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [sqldata, nosqldata, rabbitmq, identity-api]

  locations-api:
    build: services/location-service
    ports: ["5109:5109"]
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://nosqldata:27017/LocationsDb
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [nosqldata, rabbitmq, identity-api]

  webhooks-api:
    build: services/webhooks-service
    ports: ["5113:5113"]
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqldata:1433;databaseName=WebhooksDb;encrypt=false
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [sqldata, rabbitmq, identity-api]

  # Aggregators
  webshoppingagg:
    build: gateways/web-shopping-aggregator
    ports: ["5121:5121"]
    environment:
      URLS_BASKET: http://basket-api:5103
      URLS_CATALOG: http://catalog-api:5101
      URLS_ORDERS: http://ordering-api:5102
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [basket-api, catalog-api, ordering-api]

  mobileshoppingagg:
    build: gateways/mobile-shopping-aggregator
    ports: ["5122:5122"]
    environment:
      URLS_BASKET: http://basket-api:5103
      URLS_CATALOG: http://catalog-api:5101
      URLS_ORDERS: http://ordering-api:5102
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [basket-api, catalog-api, ordering-api]

  # Envoy gateways
  webshoppingapigw:
    image: envoyproxy/envoy:v1.29-latest
    volumes:
      - ./gateways/envoy/webshopping/envoy.yaml:/etc/envoy/envoy.yaml
    ports: ["5200:80", "8080:8080"]

  webmarketingapigw:
    image: envoyproxy/envoy:v1.29-latest
    volumes:
      - ./gateways/envoy/webmarketing/envoy.yaml:/etc/envoy/envoy.yaml
    ports: ["5201:80"]

  mobileshoppingapigw:
    image: envoyproxy/envoy:v1.29-latest
    volumes:
      - ./gateways/envoy/mobileshopping/envoy.yaml:/etc/envoy/envoy.yaml
    ports: ["5202:80"]

  mobilemarketingapigw:
    image: envoyproxy/envoy:v1.29-latest
    volumes:
      - ./gateways/envoy/mobilemarketing/envoy.yaml:/etc/envoy/envoy.yaml
    ports: ["5203:80"]

  # Web applications
  webmvc:
    build: web/webmvc-app
    ports: ["5100:5100"]
    environment:
      WEBSHOPPINGAGG_URL: http://webshoppingapigw:80
      WEBMARKETINGAGG_URL: http://webmarketingapigw:80
      ORDERING_URL: http://webshoppingapigw:80
      IDENTITY_URL: http://host.docker.internal:5105
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_ESHOP_ISSUER_URI: http://identity-api:5105/realms/eshop
    depends_on: [identity-api, webshoppingapigw, webmarketingapigw]

  webspa:
    build: web/webspa-host
    ports: ["5104:5104"]
    environment:
      SPA_PURCHASE_URL: http://host.docker.internal:5200
      SPA_MARKETING_URL: http://host.docker.internal:5201
      SPA_IDENTITY_URL: http://host.docker.internal:5105
      SPA_SIGNALR_HUB_URL: http://host.docker.internal:5112

  webstatus:
    build: web/webstatus-app
    ports: ["5107:5107"]
    environment:
      MONITORED_SERVICES: "catalog-api:5101,basket-api:5103,ordering-api:5102,identity-api:5105"

  webhooks-client:
    build: web/webhook-client-app
    ports: ["5114:5114"]
    environment:
      WEBHOOKS_API_URL: http://webhooks-api:5113
      SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_ESHOP_ISSUER_URI: http://identity-api:5105/realms/eshop

volumes:
  sqldata:
  nosqldata:
  basketdata:
```

### Port Reference

| Service | Port |
|---------|------|
| WebMVC | 5100 |
| Catalog API | 5101 |
| Ordering API | 5102 |
| Basket API | 5103 |
| WebSPA | 5104 |
| Identity API (Keycloak) | 5105 |
| WebStatus | 5107 |
| Payment API | 5108 |
| Locations API | 5109 |
| Marketing API | 5110 |
| Ordering BackgroundTasks | 5111 |
| Ordering SignalR Hub | 5112 |
| Webhooks API | 5113 |
| Webhooks Client | 5114 |
| Web Shopping Gateway (Envoy) | 5200 |
| Web Marketing Gateway (Envoy) | 5201 |
| Mobile Shopping Gateway (Envoy) | 5202 |
| Mobile Marketing Gateway (Envoy) | 5203 |
| Web Shopping Aggregator | 5121 |
| Mobile Shopping Aggregator | 5122 |
| Catalog gRPC | 9101 |
| Ordering gRPC | 9102 |
| Basket gRPC | 9103 |

---

## 16. Testing Strategy

### 16.1 Unit Tests

Each service module includes `src/test/java/` with:

| Component | Test approach |
|-----------|--------------|
| Domain entities (`Order`, `OrderItem`, `Buyer`, `CatalogItem`) | Plain JUnit 5 — no Spring context |
| Command handlers | Mockito for repository mocks |
| Controllers | `@WebMvcTest` + MockMvc |
| Integration event handlers | Mockito for event bus mock |
| Redis repository | Testcontainers Redis |
| SQL repositories | `@DataJpaTest` with H2 + `h2_schema.sql` |

**Catalog unit tests** (matching `Catalog.UnitTests`):
- `CatalogItemRemoveStockTest`: verify `removeStock` throws on zero stock, negative quantity; correctly reduces `availableStock`.
- `CatalogItemAddStockTest`: verify caps at `maxStockThreshold`, clears `onReorder`.

**Ordering unit tests** (matching `Ordering.UnitTests`):
- `OrderAggregateTest`: status transitions, `addOrderItem` merging, `getTotal`.
- `BuyerAggregateTest`: `verifyOrAddPaymentMethod` reuse vs. create.
- `OrderItemTest`: constructor domain exceptions.
- `CreateOrderCommandHandlerTest`: creates order with correct fields.
- `CancelOrderCommandHandlerTest`: returns false when order not in cancellable status.
- `ShipOrderCommandHandlerTest`: transitions status to shipped.
- `IdentifiedCommandHandlerTest`: deduplication on duplicate request id.

### 16.2 Integration Tests

Use `@SpringBootTest` + Testcontainers:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class CatalogApiIntegrationTest {
    @Container
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest");
    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");
}
```

### 16.3 Acceptance Tests

`tests/application-acceptance-tests` — uses Docker Compose or Testcontainers Compose to start the full stack and verifies end-to-end scenarios from `C:\Projects\Spring-eShopOnContainers\TEST_SCENARIOS.md`.

Key scenarios:
1. Anonymous user browses catalog → sees paginated items.
2. User logs in → basket empty.
3. User adds item to basket → basket has item.
4. User checks out → order created in `submitted` status.
5. Order progresses through: awaiting validation → stock confirmed → paid → shipped.
6. Order cancellation from awaiting validation.
7. Catalog price change updates basket item `oldUnitPrice`.
8. Webhook subscription receives `OrderPaid` notification.

---

## 17. Framework Mapping Reference

| .NET Pattern | Java/Spring Equivalent | Notes |
|-------------|----------------------|-------|
| `[Route("api/v1/[controller]")]` | `@RequestMapping("/api/v1/catalog")` | Use explicit path, not variable |
| `[HttpGet]`, `[HttpPost]`, etc. | `@GetMapping`, `@PostMapping`, etc. | |
| `[FromBody]` | `@RequestBody` | |
| `[FromQuery]` | `@RequestParam` | |
| `[FromRoute]` | `@PathVariable` | |
| `[FromHeader]` | `@RequestHeader` | |
| `IActionResult` | `ResponseEntity<?>` | |
| `Ok(value)` | `ResponseEntity.ok(value)` | |
| `NotFound()` | `ResponseEntity.notFound().build()` | |
| `BadRequest(msg)` | `ResponseEntity.badRequest().body(msg)` | |
| `CreatedAtAction(...)` | `ResponseEntity.created(uri).build()` | |
| `Accepted()` | `ResponseEntity.accepted().build()` | |
| `NoContent()` | `ResponseEntity.noContent().build()` | |
| `[ApiController]` | `@RestController` + `@Validated` | |
| `DbContext.SaveChangesAsync()` | `repository.save(entity)` | |
| `LINQ .Where().Skip().Take()` | JPA `Pageable` + `findBy*` | |
| `IOptionsSnapshot<T>` | `@ConfigurationProperties` bean | |
| EF Core owned entity (Address) | `@Embedded` + `@Embeddable` | |
| EF Core table-per-hierarchy | `@Inheritance(SINGLE_TABLE)` | |
| EF Core HiLo sequence | `IDENTITY` column | Matches H2 schema |
| `MediatR.Send(command)` | `commandBus.send(command)` or direct `@Service` call | |
| `INotification` domain event | Spring `ApplicationEvent` or custom domain event list | |
| `IHostedService` | `@Component` implementing `CommandLineRunner` or `@Scheduled` | |
| `BackgroundService` | `@Scheduled` method or `@Component` with `@PostConstruct` thread | |
| `Task<T>` | `CompletableFuture<T>` or synchronous in Spring AMQP handlers | Spring AMQP listeners are synchronous by default |
| Razor view `@model` | Thymeleaf `th:object="${model}"` | |
| Razor `@Html.AntiForgeryToken()` | Thymeleaf `th:action` auto-includes CSRF | Spring Security CSRF filter |
| SignalR Hub | Spring WebSocket `@MessageMapping` | |
| Polly `WaitAndRetryAsync` | `@Retryable` (Spring Retry) or Resilience4j `Retry` | |
| `ILogger<T>` | `private static final Logger log = LoggerFactory.getLogger(T.class)` | SLF4J |
| xUnit `[Fact]` | `@Test` (JUnit 5) | |
| xUnit `[Theory]` | `@ParameterizedTest` | |
| Moq `Mock<T>` | `@Mock` / `Mockito.mock(T.class)` | |
| `HttpClient` | `WebClient` (reactive) or `RestTemplate` / `RestClient` | |
| Swashbuckle | `springdoc-openapi-starter-webmvc-ui` | `GET /swagger-ui.html` |

---

## 18. Implementation Order

Build in this order so each step has its dependencies available:

### Phase 1 — Common Infrastructure (Week 1)

1. Parent `pom.xml` with Spring Boot BOM and version management.
2. `docker-compose.yml` with SQL Server, Redis, MongoDB, RabbitMQ, Seq, Keycloak.
3. `common/event-bus` — RabbitMQ implementation, event envelope, publisher, listener registration.
4. `common/event-log` — outbox table, service interface, JPA implementation.
5. `common/web-support` — `PaginatedItemsViewModel`, `GlobalExceptionHandler`, startup helpers.
6. `common/security` — `IdentityService`, JWT config.
7. Keycloak realm config with demo user, clients, and scopes.

### Phase 2 — Core Data Services (Week 2)

8. `catalog-service` — JPA entities, Flyway migration, REST controller, seed data, health endpoint.
9. `basket-service` — Redis repository, REST controller, checkout event publish.
10. Verify `GET /api/v1/catalog/items` and `GET /api/v1/basket/{id}` return correct shapes.

### Phase 3 — Ordering (Week 3)

11. `ordering-service` domain layer — `Order`, `OrderItem`, `Buyer`, `PaymentMethod`, value objects, domain events.
12. `ordering-service` infrastructure — JPA mappings, `OrderRepository`, `BuyerRepository`, `OrderQueries`.
13. `ordering-service` application — command handlers, `TransactionBehavior`, `IdentifiedCommandHandler`.
14. `ordering-service` REST controller — `/api/v1/orders` routes.
15. Wire integration event handlers for checkout, grace period, stock, payment events.
16. `ordering-background-service` — grace period scheduler.

### Phase 4 — Checkout Flow End-to-End (Week 4)

17. `payment-service` — consume stock-confirmed, publish payment result.
18. Catalog integration event handlers — stock validation, price change.
19. Basket integration event handlers — clear on order start, update on price change.
20. Verify full checkout saga: basket checkout → order submitted → awaiting validation → stock confirmed → paid.

### Phase 5 — Aggregators and WebMVC (Week 5)

21. `web-shopping-aggregator` — basket composition, catalog enrichment, order draft.
22. `mobile-shopping-aggregator` — same routes, different add-item behavior.
23. Envoy gateway config pointing to Spring services.
24. `webmvc-app` — Thymeleaf templates, Spring Security OAuth2 client, service calls through aggregator.
25. Verify catalog browse → add to cart → checkout → order history in browser.

### Phase 6 — Supporting Services (Week 6)

26. `location-service` — MongoDB location documents, REST, `UserLocationUpdatedIntegrationEvent`.
27. `marketing-service` — Campaign CRUD, rule management, user campaign selection, location event consumer.
28. `webhooks-service` — subscription CRUD, event consumer, HTTP dispatch.
29. `webhook-client-app` — subscription UI, webhook receiver.

### Phase 7 — Real-Time Notifications (Week 7)

30. `ordering-notification-service` — STOMP WebSocket hub, order status consumer.
31. Update `webmvc-app` to subscribe to STOMP notifications for real-time order updates.
32. `webspa-host` — serve Angular app, inject configuration.

### Phase 8 — Status Dashboard and Polish (Week 8)

33. `webstatus-app` — aggregate health checks, `/Config` endpoint.
34. Springdoc OpenAPI on every API service.
35. `/hc` and `/liveness` on every service.
36. Full acceptance test suite.
37. H2 test schema wired into all service test configs.

---

## Reference Documents

All detailed contracts from the prior Spring-eShopOnContainers analysis are preserved in `C:\Projects\Spring-eShopOnContainers\`:

| File | Contents |
|------|---------|
| `API_CONTRACTS.md` | Complete REST, BFF, MVC, and webhook route specifications with request/response shapes |
| `DATA_MODEL.md` | Full table definitions, column types, constraints, indexes, and seed data |
| `EVENT_CONTRACTS.md` | RabbitMQ topology, outbox behavior, retry semantics, all event payload shapes |
| `UI_BEHAVIOR.md` | Page-by-page UI behavior for WebMVC and WebSPA |
| `DOTNET_TO_SPRING_MAPPING.md` | Project-level mapping table |
| `AUTH_SECURITY.md` | Detailed OIDC/OAuth2 flow and claims mapping |
| `BUSINESS_RULES.md` | Domain invariants and business rules per service |
| `TEST_SCENARIOS.md` | Acceptance test backlog |
| `NON_FUNCTIONAL_REQUIREMENTS.md` | Resiliency, health check, logging NFRs |
| `h2_schema.sql` | H2-compatible DDL for all tables |
| `h2_data.sql` | Seed data inserts |
