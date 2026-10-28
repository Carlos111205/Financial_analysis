package com.accounting.revenuerecognition.web;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Serves the interactive Financial Dashboard
        return "forward:/index.html";
    }

    @GetMapping("/swagger")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/favicon.ico")
    public void favicon() {
        // No-op to prevent unneeded 404 logs for browser favicon polling
    }
}

