package com.eshop.webmvc.web.controllers;

import com.eshop.webmvc.config.AuthHelper;
import com.eshop.webmvc.model.OrderDetails;
import com.eshop.webmvc.model.OrderSummary;
import com.eshop.webmvc.services.OrderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderingService orderingService;
    private final AuthHelper authHelper;

    @GetMapping
    public String orders(Authentication auth, Model model) {
        String accessToken = authHelper.getAccessToken(auth);

        try {
            List<OrderSummary> orders = orderingService.getOrders(accessToken);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            log.error("Error loading orders: {}", e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("errorMsg", "Could not load orders. Please try again later.");
        }
        return "orders/index";
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable int orderId, Authentication auth, Model model) {
        String accessToken = authHelper.getAccessToken(auth);

        try {
            OrderDetails order = orderingService.getOrder(orderId, accessToken);
            model.addAttribute("order", order);
        } catch (Exception e) {
            log.error("Error loading order {}: {}", orderId, e.getMessage());
            model.addAttribute("errorMsg", "Could not load order details.");
        }
        return "orders/detail";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable int orderId, Authentication auth) {
        String accessToken = authHelper.getAccessToken(auth);
        boolean result = orderingService.cancelOrder(orderId, UUID.randomUUID(), accessToken);
        if (!result) {
            log.warn("Failed to cancel order: {}", orderId);
        }
        return "redirect:/orders";
    }
}
