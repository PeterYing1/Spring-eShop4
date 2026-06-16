package com.eshop.webhookclient.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebhookData {

    private String subscriptionType;
    private String payload;
    private String destUrl;
    private String token;
}
