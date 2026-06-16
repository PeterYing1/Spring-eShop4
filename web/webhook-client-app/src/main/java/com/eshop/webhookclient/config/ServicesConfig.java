package com.eshop.webhookclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Data
@Configuration
@ConfigurationProperties(prefix = "services")
@EnableConfigurationProperties({WebhookClientProperties.class})
public class ServicesConfig {

    private String webhooksUrl = "http://webhooks-api:5113";

    /**
     * RestTemplate pre-configured with the webhooks-api base URL.
     */
    @Bean(name = "webhooksRestTemplate")
    public RestTemplate webhooksRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(webhooksUrl));
        return restTemplate;
    }
}
