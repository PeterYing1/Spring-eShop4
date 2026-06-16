package com.eshop.webmvc.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(ServicesUrlsConfig.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final ServicesUrlsConfig servicesUrlsConfig;

    public WebMvcConfig(ServicesUrlsConfig servicesUrlsConfig) {
        this.servicesUrlsConfig = servicesUrlsConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/");
    }

    @Bean(name = "catalogClient")
    public WebClient catalogClient() {
        return WebClient.builder()
                .baseUrl(servicesUrlsConfig.getCatalogUrl())
                .build();
    }

    @Bean(name = "basketClient")
    public WebClient basketClient() {
        return WebClient.builder()
                .baseUrl(servicesUrlsConfig.getBasketUrl())
                .build();
    }

    @Bean(name = "orderClient")
    public WebClient orderClient() {
        return WebClient.builder()
                .baseUrl(servicesUrlsConfig.getOrderingUrl())
                .build();
    }
}
