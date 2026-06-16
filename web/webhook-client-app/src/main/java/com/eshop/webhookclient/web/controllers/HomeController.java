package com.eshop.webhookclient.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirects the root URL to the webhooks dashboard.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/webhooks";
    }
}
