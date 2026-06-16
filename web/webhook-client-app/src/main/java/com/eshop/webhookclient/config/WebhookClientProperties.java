package com.eshop.webhookclient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "webhook-client")
public class WebhookClientProperties {

    /** Secret token used to validate incoming webhook callbacks. */
    private String token = "secret-token";

    /** The public callback URL registered with the webhooks-service. */
    private String callbackUrl = "http://webhookclient:5114/webhook/receive";
}
