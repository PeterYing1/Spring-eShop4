package com.eshop.webstatus.web;

import com.eshop.webstatus.model.ServiceStatus;
import com.eshop.webstatus.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/")
    public String index(Model model) {
        List<ServiceStatus> services = healthCheckService.checkAll();
        model.addAttribute("services", services);
        model.addAttribute("renderedAt", Instant.now());
        return "index";
    }
}
