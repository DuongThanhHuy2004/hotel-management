package com.hotel.controller;

import com.hotel.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.hotel.dto.ContactDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import com.hotel.dto.PasswordChangeDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Controller
public class PageController {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final ServiceService serviceService;
    private final ContactService contactService;
    private final DashboardService dashboardService;
    private final UserService userService;
    private final ReviewService reviewService;

    public PageController(RoomService roomService, BookingService bookingService, ServiceService serviceService, ContactService contactService, DashboardService dashboardService, UserService userService, ReviewService reviewService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.serviceService = serviceService;
        this.contactService = contactService;
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    private String getLoggedInUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof DefaultOAuth2User) {
            return ((DefaultOAuth2User) principal).getAttribute("email");
        } else {
            return principal.toString(); // Fallback
        }
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                    return "redirect:/admin/dashboard";
                }
            }
        }
        // 1. Lấy 3 phòng mới nhất
        model.addAttribute("featuredRooms", roomService.findTop3ByOrderByIdDesc());

        // 2. Lấy 3 review mới nhất
        model.addAttribute("latestReviews", reviewService.findTop3ByOrderByCreatedAtDesc());
        return "client/index"; // Trang chủ cho client (user và khách)
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Gửi 4 con số thống kê ra view
        model.addAttribute("summary", dashboardService.getDashboardSummary());
        return "admin/index";
    }

    @GetMapping("/rooms")
    public String clientRoomsPage(
            // Thêm 2 tham số này, không bắt buộc
            @RequestParam(name = "checkInDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,

            @RequestParam(name = "checkOutDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,

            Model model) {

        if (checkInDate != null && checkOutDate != null) {
            // Nếu có ngày tìm kiếm -> Lọc
            model.addAttribute("rooms", roomService.findAvailableRooms(checkInDate, checkOutDate));
        } else {
            // Nếu không có ngày (vào thẳng /rooms) -> Hiển thị tất cả
            model.addAttribute("rooms", roomService.findAll());
        }

        // Gửi lại 2 biến ngày ra view để hiển thị lại trên form
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);

        return "client/rooms";
    }

    @GetMapping("/room-details/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("room", roomService.findById(id));
            model.addAttribute("allServices", serviceService.findAll());
            model.addAttribute("reviews", reviewService.getReviewsForRoom(id));
            model.addAttribute("averageRating", reviewService.getAverageRatingForRoom(id));
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
        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        model.addAttribute("bookings", bookingService.findBookingsByUsername(username));
        return "client/my-bookings";
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

    // 1. Hiển thị trang Profile
    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("passwordDto", new PasswordChangeDto());
        return "client/profile"; // (Sẽ tạo ở bước 5)
    }

    // 2. Xử lý đổi mật khẩu
    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        // 2.1. Kiểm tra 2 mật khẩu mới có khớp không
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(username, passwordDto.getOldPassword(), passwordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}