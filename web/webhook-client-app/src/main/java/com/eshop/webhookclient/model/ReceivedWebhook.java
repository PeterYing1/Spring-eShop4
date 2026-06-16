package com.eshop.webhookclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ReceivedWebhook {

    private String type;
    private String data;
    private Instant receivedAt;
}
