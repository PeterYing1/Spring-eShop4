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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final BasketService basketService;
    private final AuthHelper authHelper;

    @GetMapping
    public String cart() {
        return "redirect:/catalog";
    }

    @GetMapping("/additem")
    public String addItemGet(@RequestParam int productId,
                              @RequestParam String productName,
                              @RequestParam BigDecimal unitPrice,
                              @RequestParam(required = false) String pictureUrl,
                              Authentication auth) {
        BasketItem item = new BasketItem();
        item.setProductId(productId);
        item.setProductName(productName);
        item.setUnitPrice(unitPrice);
        item.setPictureUrl(pictureUrl);
        item.setQuantity(1);
        return addItem(item, auth);
    }

    @PostMapping("/additem")
    public String addItem(BasketItem item, Authentication auth) {
        String userId = authHelper.getUserId(auth);
        String accessToken = authHelper.getAccessToken(auth);
        log.debug("addItem: userId={} productId={} productName={}", userId, item.getProductId(), item.getProductName());

        try {
            CustomerBasket basket = basketService.getBasket(userId, accessToken);

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
                if (item.getQuantity() <= 0) item.setQuantity(1);
                basket.getItems().add(item);
            }

            basketService.updateBasket(basket, accessToken);
            log.debug("addItem: basket updated, found={}", found);
        } catch (Exception e) {
            log.error("Error adding item to basket: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
        return "redirect:/catalog";
    }

    @GetMapping("/updateqty")
    public String updateQty(@RequestParam int productId, @RequestParam int delta, Authentication auth) {
        String userId = authHelper.getUserId(auth);
        String accessToken = authHelper.getAccessToken(auth);
        log.debug("updateqty: userId={} productId={} delta={}", userId, productId, delta);

        try {
            CustomerBasket basket = basketService.getBasket(userId, accessToken);
            log.debug("updateqty: basket has {} items", basket.getItems().size());

            java.util.List<BasketItem> updatedItems = new java.util.ArrayList<>();
            for (BasketItem i : basket.getItems()) {
                if (i.getProductId() == productId) {
                    int newQty = i.getQuantity() + delta;
                    log.debug("updateqty: item {} qty {} -> {}", i.getProductName(), i.getQuantity(), newQty);
                    if (newQty > 0) {
                        i.setQuantity(newQty);
                        updatedItems.add(i);
                    }
                } else {
                    updatedItems.add(i);
                }
            }
            basket.setItems(updatedItems);
            basketService.updateBasket(basket, accessToken);
            log.debug("updateqty: basket updated successfully");
        } catch (Exception e) {
            log.error("Error updating basket quantity: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
        return "redirect:/catalog";
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
        return "redirect:/catalog";
    }

    @GetMapping("/checkout")
    public String checkoutSummary(Authentication auth, Model model) {
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
            log.error("Error loading basket for checkout: {}", e.getMessage());
            model.addAttribute("basket", new CustomerBasket());
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("errorMsg", "Basket service is currently unavailable.");
        }
        return "cart/checkout";
    }

    @GetMapping("/payment")
    public String paymentForm(Authentication auth, Model model) {
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
            log.error("Error loading basket for payment: {}", e.getMessage());
            model.addAttribute("basket", new CustomerBasket());
            model.addAttribute("total", BigDecimal.ZERO);
        }

        model.addAttribute("cardHolderName", authHelper.getClaim(auth, "card_holder_name"));
        model.addAttribute("cardNumber", authHelper.getClaim(auth, "card_number"));
        model.addAttribute("cardExpiration", authHelper.getClaim(auth, "expiration"));
        model.addAttribute("cardSecurityNumber", authHelper.getClaim(auth, "security_number"));
        return "cart/payment";
    }

    @PostMapping("/payment")
    public String submitPayment(
            @RequestParam String cardHolderName,
            @RequestParam(defaultValue = "1") int cardTypeId,
            @RequestParam String cardNumber,
            @RequestParam String cardExpiration,
            @RequestParam String cardSecurityNumber,
            Authentication auth) {

        String accessToken = authHelper.getAccessToken(auth);

        BasketCheckout checkout = new BasketCheckout();
        checkout.setBuyer(authHelper.getUsername(auth));
        checkout.setCity(nullSafe(authHelper.getClaim(auth, "city")));
        checkout.setStreet(nullSafe(authHelper.getClaim(auth, "street")));
        checkout.setState(nullSafe(authHelper.getClaim(auth, "state")));
        checkout.setCountry(nullSafe(authHelper.getClaim(auth, "country")));
        checkout.setZipCode(nullSafe(authHelper.getClaim(auth, "zip_code")));
        checkout.setCardHolderName(cardHolderName);
        checkout.setCardNumber(cardNumber);
        checkout.setCardSecurityNumber(cardSecurityNumber);
        checkout.setCardTypeId(cardTypeId);
        checkout.setRequestId(UUID.randomUUID());

        // Convert MM/YY → ISO-8601 instant for basket API
        checkout.setCardExpiration(parseExpiration(cardExpiration));

        try {
            basketService.checkout(checkout, accessToken);
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage());
        }
        return "redirect:/orders";
    }

    private String parseExpiration(String mmYY) {
        if (mmYY == null || mmYY.isBlank()) return null;
        try {
            String[] parts = mmYY.split("/");
            int month = Integer.parseInt(parts[0].trim());
            int year = 2000 + Integer.parseInt(parts[1].trim());
            return LocalDate.of(year, month, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
