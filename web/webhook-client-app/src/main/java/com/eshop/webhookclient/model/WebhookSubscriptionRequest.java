package com.eshop.webhookclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscriptionRequest {

    private String type;
    private String url;
    private String token;
    private String grantUrl;
}
