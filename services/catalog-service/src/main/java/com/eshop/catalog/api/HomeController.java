package com.eshop.catalog.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirects the root path to the Swagger UI.
 */
@Controller
public class HomeController {

    /**
     * Redirects {@code GET /} to the Swagger UI page.
     *
     * @return a Spring MVC redirect view name
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/swagger-ui.html";
    }
}
