package com.eshop.webmvc.web.controllers;

import com.eshop.webmvc.config.AuthHelper;
import com.eshop.webmvc.model.BasketCheckout;
import com.eshop.webmvc.model.BasketItem;
import com.eshop.webmvc.model.CustomerBasket;
import com.eshop.webmvc.services.BasketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final BasketService basketService;
    private final AuthHelper authHelper;

    @GetMapping
    public String cart(Authentication auth, Model model) {
        String userId = authHelper.getUserId(auth);
        String accessToken = authHelper.getAccessToken(auth);

        try {
            CustomerBasket basket = basketService.getBasket(userId, accessToken);
            BigDecimal total = basket.getItems().stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("basket", basket);
            model.addAttribute("total", total);
        } catch (Exception e) {
            log.error("Error loading basket: {}", e.getMessage());
            model.addAttribute("basket", new CustomerBasket());
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("errorMsg", "Basket service is currently unavailable.");
        }
        return "cart/index";
    }

    @PostMapping("/additem")
    public String addItem(@ModelAttribute BasketItem item, Authentication auth) {
        String userId = authHelper.getUserId(auth);
        String accessToken = authHelper.getAccessToken(auth);

        try {
            CustomerBasket basket = basketService.getBasket(userId, accessToken);

            // Check if item already in basket; if so, increment quantity
            boolean found = false;
            for (BasketItem existing : basket.getItems()) {
                if (existing.getProductId() == item.getProductId()) {
                    existing.setQuantity(existing.getQuantity() + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                item.setId(UUID.randomUUID().toString());
                if (item.getQuantity() <= 0) {
                    item.setQuantity(1);
                }
                basket.getItems().add(item);
            }

            basketService.updateBasket(basket, accessToken);
        } catch (Exception e) {
            log.error("Error adding item to basket: {}", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/removeitem")
    public String removeItem(@RequestParam int id, Authentication auth) {
        String userId = authHelper.getUserId(auth);
        String accessToken = authHelper.getAccessToken(auth);

        try {
            CustomerBasket basket = basketService.getBasket(userId, accessToken);
            basket.getItems().removeIf(i -> i.getProductId() == id);
            basketService.updateBasket(basket, accessToken);
        } catch (Exception e) {
            log.error("Error removing item from basket: {}", e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutForm(Authentication auth, Model model) {
        BasketCheckout checkout = new BasketCheckout();

        // Pre-populate from Keycloak JWT claims
        checkout.setCity(authHelper.getClaim(auth, "city"));
        checkout.setStreet(authHelper.getClaim(auth, "street"));
        checkout.setState(authHelper.getClaim(auth, "state"));
        checkout.setCountry(authHelper.getClaim(auth, "country"));
        checkout.setZipCode(authHelper.getClaim(auth, "zip_code"));
        checkout.setCardNumber(authHelper.getClaim(auth, "card_number"));
        checkout.setCardHolderName(authHelper.getClaim(auth, "card_holder_name"));
        checkout.setCardExpiration(authHelper.getClaim(auth, "expiration"));
        checkout.setCardSecurityNumber(authHelper.getClaim(auth, "security_number"));
        checkout.setCardTypeId(1);
        checkout.setBuyer(authHelper.getUsername(auth));

        model.addAttribute("checkout", checkout);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute BasketCheckout checkout, Authentication auth) {
        String accessToken = authHelper.getAccessToken(auth);
        checkout.setBuyer(authHelper.getUsername(auth));

        try {
            basketService.checkout(checkout, accessToken);
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage());
        }
        return "redirect:/orders";
    }
}
