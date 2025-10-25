package com.hotel.controller;

import com.hotel.dto.BookingDto;
import com.hotel.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public String createBooking(@ModelAttribute BookingDto bookingDto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        // 1. Yêu cầu phải đăng nhập
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // 2. Lấy username của người đang đăng nhập
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        try {
            // 3. Gọi service để tạo booking
            bookingService.createBooking(bookingDto, username);
            redirectAttributes.addFlashAttribute("successMessage", "Your room has been booked successfully! Please wait for admin confirmation.");
            return "redirect:/rooms"; // (Hoặc trang "My Bookings" nếu có)

        } catch (RuntimeException e) {
            // 4. Nếu có lỗi (hết phòng, ngày sai...)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Quay lại trang chi tiết phòng
            return "redirect:/room-details/" + bookingDto.getRoomId();
        }
    }
}