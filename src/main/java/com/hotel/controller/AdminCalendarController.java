package com.hotel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/calendar")
public class AdminCalendarController {

    @GetMapping
    public String showCalendarPage() {
        return "admin/calendar"; // (Sẽ tạo ở bước 7)
    }
}