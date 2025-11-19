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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
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
            return null;
        }
    }

    @PostMapping("/create")
    public String createBooking(@ModelAttribute BookingDto bookingDto,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        try {
            bookingService.createBooking(bookingDto, username);
            redirectAttributes.addFlashAttribute("successMessage", "Your room has been booked successfully! Please wait for admin confirmation.");
            return "redirect:/my-bookings"; // Sửa: Về trang My Bookings

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/room-details/" + bookingDto.getRoomId();
        }
    }

    @GetMapping("/cancel/{id}")
    public String cancelMyBooking(@PathVariable("id") Long bookingId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        String username = getLoggedInUsername(authentication);
        if (username == null) {
            return "redirect:/login";
        }

        try {
            bookingService.cancelMyBooking(bookingId, username);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " has been canceled.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: You don't have permission to perform this action.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/my-bookings";
    }
}