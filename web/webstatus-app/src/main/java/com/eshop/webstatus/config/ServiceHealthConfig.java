package com.eshop.webstatus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "healthchecks")
@EnableConfigurationProperties
@Data
public class ServiceHealthConfig {

    private List<ServiceEndpoint> endpoints = new ArrayList<>();

    @Data
    public static class ServiceEndpoint {
        private String name;
        private String url;
    }
}
