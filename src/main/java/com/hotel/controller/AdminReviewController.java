package com.hotel.controller;

import com.hotel.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. Hiển thị danh sách tất cả reviews
    @GetMapping
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewService.findAll());
        return "admin/reviews"; // (Sẽ tạo ở bước 2)
    }

    // 2. Xóa một review
    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting review: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}