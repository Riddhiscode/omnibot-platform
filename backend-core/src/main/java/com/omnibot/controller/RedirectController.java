package com.omnibot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root redirect controller.
 * Since this is annotated with @Controller (not @RestController),
 * it does not get the "/api" prefix and handles requests at "/" root directly.
 */
@Controller
public class RedirectController {

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login.html";
    }
}
