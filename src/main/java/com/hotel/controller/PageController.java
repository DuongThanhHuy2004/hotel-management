package com.hotel.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                    return "redirect:/admin/dashboard";
                }
            }
        }
        return "client/index"; // Trang chủ cho client (user và khách)
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/index";
    }
}