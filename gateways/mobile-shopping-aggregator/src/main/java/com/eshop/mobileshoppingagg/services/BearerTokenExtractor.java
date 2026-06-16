package com.eshop.mobileshoppingagg.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Extracts the raw {@code Authorization: Bearer …} header value from the
 * incoming HTTP request so it can be forwarded to downstream services.
 */
@Component
public class BearerTokenExtractor {

    /**
     * Returns the full {@code Authorization} header value (e.g.
     * {@code "Bearer eyJ..."}) or {@code null} if it is absent.
     */
    public String extract(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
