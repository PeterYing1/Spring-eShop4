package com.eshop.webhookclient.services;

import com.eshop.webhookclient.model.ReceivedWebhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory store for received webhook callbacks. Capped at 20 entries (LIFO eviction).
 * Data is reset on application restart — same as the .NET demo.
 */
@Slf4j
@Component
public class ReceivedWebhookStore {

    private static final int MAX_SIZE = 20;

    private final ConcurrentLinkedDeque<ReceivedWebhook> store = new ConcurrentLinkedDeque<>();

    public synchronized void add(ReceivedWebhook webhook) {
        store.addFirst(webhook);
        while (store.size() > MAX_SIZE) {
            store.removeLast();
        }
        log.debug("Stored received webhook of type '{}', store size now {}", webhook.getType(), store.size());
    }

    public List<ReceivedWebhook> getAll() {
        return new ArrayList<>(store);
    }
}
