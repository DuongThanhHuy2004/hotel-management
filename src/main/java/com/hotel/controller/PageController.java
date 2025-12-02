package com.hotel.controller;

import com.hotel.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.userdetails.UserDetails;
import com.hotel.dto.ContactDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import com.hotel.dto.PasswordChangeDto;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hotel.entity.Booking;
import com.hotel.entity.Room;


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
            return principal.toString();
        }
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_ADMIN".equals(auth.getAuthority()) || "ROLE_STAFF".equals(auth.getAuthority())) {
                    return "redirect:/admin/dashboard";
                }
            }
        }
        // 1. Lấy 4 phòng mới nhất
        model.addAttribute("featuredRooms", roomService.findTop4ByOrderByIdDesc());

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
            @RequestParam(name = "checkInDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,

            @RequestParam(name = "checkOutDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,

            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Room> roomPage;

        if (checkInDate != null && checkOutDate != null) {
            roomPage = roomService.findAvailableRooms(checkInDate, checkOutDate, pageable);
        } else {
            roomPage = roomService.findAll(pageable);
        }

        model.addAttribute("roomPage", roomPage);
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

            return "client/room-details";
        } catch (RuntimeException ex) {
            return "redirect:/rooms?error=notFound";
        }
    }
    @GetMapping("/my-bookings")
    public String myBookings(Model model, Authentication authentication,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "6") int size) {

        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingService.findBookingsByUsername(username, pageable);

        model.addAttribute("bookingPage", bookingPage);

        return "client/my-bookings";
    }

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("contactDto", new ContactDto());
        return "client/contact";
    }

    @PostMapping("/contact/send")
    public String sendContactMessage(@ModelAttribute("contactDto") ContactDto contactDto,
                                     RedirectAttributes redirectAttributes) {
        try {
            contactService.saveContact(contactDto);
            redirectAttributes.addFlashAttribute("successMessage", "Tin nhắn của bạn đã được gửi thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi gửi tin nhắn, hãy thử lại");
        }
        return "redirect:/contact";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("passwordDto", new PasswordChangeDto());
        return "client/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không khớp");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(username, passwordDto.getOldPassword(), passwordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}