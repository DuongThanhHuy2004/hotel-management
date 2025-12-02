package com.hotel.controller;

import com.hotel.dto.RegisterDto;
import com.hotel.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@ModelAttribute("user") RegisterDto registerDto,
                               BindingResult result,
                               Model model) {
        // 1. Kiểm tra lỗi trùng lặp từ Service
        try {
            userService.saveUser(registerDto);
        } catch (RuntimeException e) {
            // Nếu có lỗi (trùng username/email), bắt lấy thông báo lỗi
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", registerDto); // Giữ lại dữ liệu đã nhập
            return "register"; // Trả về trang đăng ký (không redirect) để hiện lỗi
        }

        return "redirect:/register?success";
    }
}