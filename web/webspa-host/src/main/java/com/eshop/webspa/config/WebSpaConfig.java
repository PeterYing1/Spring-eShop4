package com.eshop.webspa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration for the Angular SPA host.
 *
 * <p>Static Angular build artifacts are served from {@code classpath:/static/} with a 1-hour
 * cache. Any URL that does not resolve to a real file is handled by {@link
 * com.eshop.webspa.web.SpaController}, which forwards the request to {@code index.html} so
 * that Angular's client-side router can take over.
 */
@Configuration
public class WebSpaConfig implements WebMvcConfigurer {

    /** Cache static assets for one hour (3 600 seconds). */
    private static final int CACHE_MAX_AGE_SECONDS = 3600;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/static")
                .setCachePeriod(CACHE_MAX_AGE_SECONDS);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map the standard Spring error path so Spring MVC can resolve it.
        registry.addViewController("/error").setViewName("forward:/index.html");
    }
}
