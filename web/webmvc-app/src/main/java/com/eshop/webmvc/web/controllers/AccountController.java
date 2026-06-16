package com.eshop.webmvc.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/account")
public class AccountController {

    /**
     * Initiates OIDC logout. Spring Security OAuth2 client handles the actual
     * redirect to the Keycloak end_session_endpoint when configured correctly.
     * The /account/signout URL is mapped as the logout URL in SecurityConfig.
     */
    @GetMapping("/signout")
    public String signout() {
        // Handled by Spring Security logout filter configured in SecurityConfig.
        // This mapping is a fallback/convenience redirect.
        return "redirect:/";
    }

    @GetMapping("/denied")
    public String denied() {
        return "account/denied";
    }
}
