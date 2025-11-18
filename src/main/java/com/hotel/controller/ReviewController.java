package com.hotel.controller;

import com.hotel.dto.ReviewDto;
import com.hotel.entity.Booking;
import com.hotel.service.BookingService;
import com.hotel.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;

    public ReviewController(ReviewService reviewService, BookingService bookingService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
    }

    // 1. Hiển thị Form viết Review
    @GetMapping("/write-review/{bookingId}")
    public String showReviewForm(@PathVariable Long bookingId, Model model, Authentication authentication) {
        Booking booking = bookingService.findById(bookingId);
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Kiểm tra bảo mật (phải là chủ đơn)
        if (!booking.getUser().getUsername().equals(username)) {
            return "redirect:/my-bookings?error=Permission denied";
        }

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setBookingId(bookingId);
        reviewDto.setRoomId(booking.getRoom().getId());

        model.addAttribute("reviewDto", reviewDto);
        model.addAttribute("roomName", booking.getRoom().getRoomType().getName()); // Hiển thị tên phòng
        return "client/review-form";
    }

    // 2. Nhận dữ liệu từ Form
    @PostMapping("/submit-review")
    public String submitReview(@ModelAttribute("reviewDto") ReviewDto reviewDto,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        try {
            reviewService.saveReview(reviewDto, username);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your review!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/my-bookings";
    }
}