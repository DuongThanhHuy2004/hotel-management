package com.hotel.controller;

import com.hotel.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.hotel.service.RoomService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.hotel.service.ServiceService;

@Controller
public class PageController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final ServiceService serviceService;

    public PageController(RoomService roomService, BookingService bookingService, ServiceService serviceService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.serviceService = serviceService;
    }

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

    @GetMapping("/rooms")
    public String clientRoomsPage(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "client/rooms"; // (Sẽ tạo view ở bước 9)
    }

    @GetMapping("/room-details/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("room", roomService.findById(id));
            model.addAttribute("allServices", serviceService.findAll());
            // (Có thể thêm các phòng khác để gợi ý, nếu muốn)
            // model.addAttribute("otherRooms", roomService.findAll().stream().limit(3).toList());
            return "client/room-details"; // <-- (Sẽ tạo ở bước 3)
        } catch (RuntimeException ex) {
            // (Nếu không tìm thấy phòng)
            return "redirect:/rooms?error=notFound";
        }
    }
    @GetMapping("/my-bookings")
    public String myBookings(Model model, Authentication authentication) {
        // 1. Kiểm tra xem đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // 2. Lấy username
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // 3. Lấy danh sách booking và đưa ra view
        model.addAttribute("bookings", bookingService.findBookingsByUsername(username));

        return "client/my-bookings"; // (Sẽ tạo ở bước 5)
    }
}