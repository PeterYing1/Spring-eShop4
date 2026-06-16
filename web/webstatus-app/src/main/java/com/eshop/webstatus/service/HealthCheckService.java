package com.eshop.webstatus.service;

import com.eshop.webstatus.config.ServiceHealthConfig;
import com.eshop.webstatus.model.ServiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {

    private final RestTemplate restTemplate;
    private final ServiceHealthConfig serviceHealthConfig;

    public List<ServiceStatus> checkAll() {
        return serviceHealthConfig.getEndpoints().stream()
                .map(this::checkEndpoint)
                .toList();
    }

    private ServiceStatus checkEndpoint(ServiceHealthConfig.ServiceEndpoint endpoint) {
        Instant checkedAt = Instant.now();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint.getUrl(), String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Health check for '{}' at {} returned {}", endpoint.getName(), endpoint.getUrl(), response.getStatusCode());
                return new ServiceStatus(
                        endpoint.getName(),
                        endpoint.getUrl(),
                        "UP",
                        "HTTP " + response.getStatusCode().value(),
                        checkedAt
                );
            } else {
                log.warn("Health check for '{}' at {} returned non-2xx status: {}",
                        endpoint.getName(), endpoint.getUrl(), response.getStatusCode());
                return new ServiceStatus(
                        endpoint.getName(),
                        endpoint.getUrl(),
                        "DOWN",
                        "HTTP " + response.getStatusCode().value(),
                        checkedAt
                );
            }
        } catch (RestClientException e) {
            log.warn("Health check for '{}' at {} failed: {}", endpoint.getName(), endpoint.getUrl(), e.getMessage());
            return new ServiceStatus(
                    endpoint.getName(),
                    endpoint.getUrl(),
                    "DOWN",
                    e.getMessage() != null ? truncate(e.getMessage(), 120) : "Connection failed",
                    checkedAt
            );
        } catch (Exception e) {
            log.error("Unexpected error checking '{}' at {}: {}", endpoint.getName(), endpoint.getUrl(), e.getMessage(), e);
            return new ServiceStatus(
                    endpoint.getName(),
                    endpoint.getUrl(),
                    "UNKNOWN",
                    e.getMessage() != null ? truncate(e.getMessage(), 120) : "Unknown error",
                    checkedAt
            );
        }
    }

    private String truncate(String message, int maxLength) {
        return message.length() <= maxLength ? message : message.substring(0, maxLength) + "...";
    }
}
