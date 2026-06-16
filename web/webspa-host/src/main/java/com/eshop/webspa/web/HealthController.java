package com.eshop.webspa.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight health-check endpoints used by Docker / Kubernetes probes and the
 * WebStatus dashboard.
 *
 * <p>Spring Boot Actuator also exposes a richer {@code /hc} endpoint (mapped via
 * {@code management.endpoints.web.path-mapping.health=hc} in {@code application.yml}).
 * These controller methods act as a complementary, always-available fallback that exactly
 * matches the response shape expected by the eShop health-check dashboard.
 */
@RestController
public class HealthController {

    /**
     * Composite health-check endpoint — mirrors the shape expected by
     * {@code HealthChecks.UI} in the .NET original.
     *
     * @return {@code {"status":"Healthy"}} with HTTP 200
     */
    @GetMapping("/hc")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "Healthy"));
    }

    /**
     * Liveness probe — returns HTTP 200 when the application process is alive.
     */
    @GetMapping("/liveness")
    public ResponseEntity<Void> liveness() {
        return ResponseEntity.ok().build();
    }
}
