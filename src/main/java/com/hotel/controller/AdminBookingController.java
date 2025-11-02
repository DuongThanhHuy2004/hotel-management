package com.hotel.controller;

import com.hotel.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.hotel.entity.Booking;
import com.hotel.service.ServiceService;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;
    private final ServiceService serviceService;

    public AdminBookingController(BookingService bookingService, ServiceService serviceService) {
        this.bookingService = bookingService;
        this.serviceService = serviceService;
    }

    @GetMapping
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "admin/bookings"; // (Sẽ tạo ở bước 9)
    }

    // 1. Hiển thị trang quản lý dịch vụ cho 1 đơn
    @GetMapping("/{bookingId}/services")
    public String showBookingServicesPage(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.findById(bookingId);
        model.addAttribute("booking", booking);
        // Lấy tất cả dịch vụ để admin chọn
        model.addAttribute("allServices", serviceService.findAll());
        return "admin/booking-services"; // (Sẽ tạo ở bước 5)
    }

    // 2. Hành động Thêm dịch vụ vào đơn
    @PostMapping("/add-service")
    public String addServiceToBooking(@RequestParam("bookingId") Long bookingId,
                                      @RequestParam("serviceId") Long serviceId,
                                      RedirectAttributes redirectAttributes) {
        try {
            bookingService.addServiceToBooking(bookingId, serviceId);
            redirectAttributes.addFlashAttribute("successMessage", "Service added and total price updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings/" + bookingId + "/services";
    }

    // 3. Hành động Xóa dịch vụ khỏi đơn
    @GetMapping("/{bookingId}/remove-service/{serviceId}")
    public String removeServiceFromBooking(@PathVariable Long bookingId,
                                           @PathVariable Long serviceId,
                                           RedirectAttributes redirectAttributes) {
        try {
            bookingService.removeServiceFromBooking(bookingId, serviceId);
            redirectAttributes.addFlashAttribute("successMessage", "Service removed and total price updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings/" + bookingId + "/services";
    }

    @GetMapping("/confirm/{id}")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking canceled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}