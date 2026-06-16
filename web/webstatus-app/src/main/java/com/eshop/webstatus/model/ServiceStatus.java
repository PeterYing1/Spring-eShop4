package com.eshop.webstatus.model;

import java.time.Instant;

public record ServiceStatus(
        String name,
        String url,
        String status,
        String description,
        Instant checkedAt
) {
}
