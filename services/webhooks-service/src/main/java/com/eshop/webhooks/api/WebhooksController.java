package com.eshop.webhooks.api;

import com.eshop.security.IIdentityService;
import com.eshop.webhooks.domain.WebhookSubscription;
import com.eshop.webhooks.infrastructure.WebhookSubscriptionRepository;
import com.eshop.webhooks.services.WebhooksRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * REST controller for webhook subscription management.
 *
 * <p>All endpoints require authentication ({@code @PreAuthorize("isAuthenticated()")}).
 * User ownership is always enforced — a user can only see and manage their own
 * subscriptions.
 *
 * <h3>Routes</h3>
 * <ul>
 *   <li>{@code GET  /api/v1/webhooks}      — list current user's subscriptions</li>
 *   <li>{@code GET  /api/v1/webhooks/{id}} — get a specific subscription</li>
 *   <li>{@code POST /api/v1/webhooks}      — create a new subscription</li>
 *   <li>{@code DELETE /api/v1/webhooks/{id}} — delete a subscription</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class WebhooksController {

    private final WebhookSubscriptionRepository repository;
    private final WebhooksRetriever webhooksRetriever;
    private final IIdentityService identityService;

    /**
     * Returns all webhook subscriptions belonging to the currently authenticated user.
     *
     * @return {@code 200 OK} with the list of subscriptions
     */
    @GetMapping
    public ResponseEntity<List<WebhookSubscription>> listByUser() {
        String userId = identityService.getUserIdentity();
        log.debug("Listing webhook subscriptions for user '{}'", userId);
        return ResponseEntity.ok(webhooksRetriever.getUserSubscriptions(userId));
    }

    /**
     * Returns a single webhook subscription by id, scoped to the current user.
     *
     * @param id the subscription primary key
     * @return {@code 200 OK} with the subscription, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebhookSubscription> getByUserAndId(@PathVariable int id) {
        String userId = identityService.getUserIdentity();
        var found = repository.findByIdAndUserId(id, userId);
        if (found.isEmpty()) {
            log.debug("Subscription {} not found for user '{}'", id, userId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found.get());
    }

    /**
     * Creates a new webhook subscription for the currently authenticated user.
     *
     * @param request the subscription details (type, url, token)
     * @return {@code 201 Created} with a {@code Location} header pointing to the new resource
     */
    @PostMapping
    public ResponseEntity<WebhookSubscription> subscribe(@RequestBody WebhookSubscriptionRequest request) {
        String userId = identityService.getUserIdentity();
        log.info("Creating webhook subscription type='{}' destUrl='{}' for user '{}'",
                request.getType(), request.getUrl(), userId);

        WebhookSubscription subscription = new WebhookSubscription();
        subscription.setType(request.getType());
        subscription.setDestUrl(request.getUrl());
        subscription.setToken(request.getToken());
        subscription.setUserId(userId);
        subscription.setCreatedAt(Instant.now());

        WebhookSubscription saved = repository.save(subscription);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Deletes a webhook subscription by id, scoped to the current user.
     *
     * @param id the subscription primary key
     * @return {@code 204 No Content} on success, {@code 404 Not Found} if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(@PathVariable int id) {
        String userId = identityService.getUserIdentity();
        var found = repository.findByIdAndUserId(id, userId);
        if (found.isEmpty()) {
            log.debug("Subscription {} not found for user '{}' — cannot delete", id, userId);
            return ResponseEntity.notFound().build();
        }
        repository.delete(found.get());
        log.info("Deleted webhook subscription {} for user '{}'", id, userId);
        return ResponseEntity.noContent().build();
    }
}
