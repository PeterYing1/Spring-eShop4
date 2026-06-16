package com.eshop.webmvc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "services")
public class ServicesUrlsConfig {
    private String catalogUrl;
    private String basketUrl;
    private String orderingUrl;
}
