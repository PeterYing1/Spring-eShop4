package com.eshop.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the security module.
 *
 * <p>Automatically registers {@link IdentityService} when no other
 * {@link IIdentityService} bean has been declared by the application.
 */
@AutoConfiguration
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IIdentityService.class)
    public IIdentityService identityService() {
        return new IdentityService();
    }
}
