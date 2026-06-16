package com.eshop.webhookclient.web.controllers;

import com.eshop.webhookclient.config.WebhookClientProperties;
import com.eshop.webhookclient.model.ReceivedWebhook;
import com.eshop.webhookclient.model.WebhookSubscriptionRequest;
import com.eshop.webhookclient.services.ReceivedWebhookStore;
import com.eshop.webhookclient.services.WebhooksClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * MVC controller for the webhooks subscription management dashboard.
 */
@Slf4j
@Controller
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhooksController {

    private final WebhooksClientService webhooksClientService;
    private final ReceivedWebhookStore receivedWebhookStore;
    private final WebhookClientProperties webhookClientProperties;

    /**
     * Renders the main webhooks dashboard: subscriptions list, subscribe form, received webhooks.
     */
    @GetMapping
    public String index(Authentication auth, Model model) {
        String accessToken = extractAccessToken(auth);
        String username = extractUsername(auth);

        List<Map<String, Object>> subscriptions = List.of();
        if (accessToken != null) {
            subscriptions = webhooksClientService.getSubscriptions(accessToken);
        }

        List<ReceivedWebhook> received = receivedWebhookStore.getAll();

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("received", received);
        model.addAttribute("username", username);
        model.addAttribute("callbackUrl", webhookClientProperties.getCallbackUrl());
        model.addAttribute("subscribeRequest", new WebhookSubscriptionRequest(
                "OrderPaid",
                webhookClientProperties.getCallbackUrl(),
                webhookClientProperties.getToken(),
                webhookClientProperties.getCallbackUrl()
        ));

        return "webhooks/index";
    }

    /**
     * Handles the "Subscribe to webhook" form submission.
     */
    @PostMapping("/subscribe")
    public String subscribe(@ModelAttribute WebhookSubscriptionRequest request, Authentication auth) {
        String accessToken = extractAccessToken(auth);
        if (accessToken == null) {
            log.warn("Cannot subscribe: user is not authenticated or token is unavailable");
            return "redirect:/webhooks";
        }

        log.info("Subscribing to webhook type '{}' at url '{}'", request.getType(), request.getUrl());
        boolean success = webhooksClientService.subscribe(
                request.getType(),
                request.getUrl(),
                request.getToken(),
                accessToken
        );

        if (!success) {
            log.warn("Webhook subscription failed for type '{}'", request.getType());
        }

        return "redirect:/webhooks";
    }

    /**
     * Handles the "Unsubscribe" form action (POST to /webhooks/{id}/delete).
     * HTML forms do not support DELETE, so we use a POST with a /delete suffix.
     */
    @PostMapping("/{id}/delete")
    public String unsubscribe(@PathVariable int id, Authentication auth) {
        String accessToken = extractAccessToken(auth);
        if (accessToken == null) {
            log.warn("Cannot unsubscribe: user is not authenticated or token is unavailable");
            return "redirect:/webhooks";
        }

        log.info("Unsubscribing from webhook id {}", id);
        boolean success = webhooksClientService.unsubscribe(id, accessToken);

        if (!success) {
            log.warn("Webhook unsubscribe failed for id {}", id);
        }

        return "redirect:/webhooks";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String extractAccessToken(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken token) {
            if (token.getPrincipal() instanceof OidcUser oidcUser) {
                // Prefer the raw access_token attribute set by OAuth2LoginAuthenticationFilter
                Object rawToken = oidcUser.getAttribute("access_token");
                if (rawToken instanceof String s && !s.isBlank()) {
                    return s;
                }
                // Fall back to the ID token value
                if (oidcUser.getIdToken() != null) {
                    return oidcUser.getIdToken().getTokenValue();
                }
            }
        }
        return null;
    }

    private String extractUsername(Authentication auth) {
        if (auth == null) {
            return "anonymous";
        }
        if (auth instanceof OAuth2AuthenticationToken token) {
            Object preferred = token.getPrincipal().getAttribute("preferred_username");
            if (preferred instanceof String s) {
                return s;
            }
        }
        return auth.getName();
    }
}
