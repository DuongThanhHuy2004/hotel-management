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
import com.hotel.dto.ContactDto;
import com.hotel.service.ContactService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.hotel.service.DashboardService;

@Controller
public class PageController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final ServiceService serviceService;
    private final ContactService contactService;
    private final DashboardService dashboardService;

    public PageController(RoomService roomService, BookingService bookingService, ServiceService serviceService, ContactService contactService, DashboardService dashboardService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.serviceService = serviceService;
        this.contactService = contactService;
        this.dashboardService = dashboardService;
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
    public String adminDashboard(Model model) {
        // Gửi 4 con số thống kê ra view
        model.addAttribute("summary", dashboardService.getDashboardSummary());
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
    // 1. Hiển thị trang Contact
    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("contactDto", new ContactDto());
        return "client/contact"; // (Sẽ tạo ở bước 9)
    }

    // 2. Nhận dữ liệu từ Form Contact
    @PostMapping("/contact/send")
    public String sendContactMessage(@ModelAttribute("contactDto") ContactDto contactDto,
                                     RedirectAttributes redirectAttributes) {
        try {
            contactService.saveContact(contactDto);
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error sending message. Please try again.");
        }
        return "redirect:/contact";
    }
}