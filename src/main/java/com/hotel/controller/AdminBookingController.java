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
import com.hotel.entity.HotelService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

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
    public String listBookings(Model model,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size) {

        // Tạo đối tượng Pageable (trang hiện tại, số lượng mỗi trang)
        Pageable pageable = PageRequest.of(page, size);

        // Gọi service đã được phân trang
        Page<Booking> bookingPage = bookingService.findAll(pageable);

        // Gửi đối tượng Page ra view
        model.addAttribute("bookingPage", bookingPage);

        return "admin/bookings";
    }

    // 1. Hiển thị trang quản lý dịch vụ cho 1 đơn
    @GetMapping("/{bookingId}/services")
    public String showBookingServicesPage(@PathVariable Long bookingId, Model model) {
        // 1. Lấy booking (đã chứa các service đã thêm)
        Booking booking = bookingService.findById(bookingId);
        model.addAttribute("booking", booking);

        // 2. Lấy TẤT CẢ dịch vụ
        List<HotelService> allServices = serviceService.findAll();

        // 3. Lấy ra ID của các dịch vụ ĐÃ CÓ trong booking
        Set<Long> addedServiceIds = booking.getServices().stream()
                .map(HotelService::getId)
                .collect(Collectors.toSet());

        // 4. Lọc và tạo ra một danh sách dịch vụ MỚI (chỉ chứa các dịch vụ CHƯA CÓ)
        List<HotelService> availableServices = allServices.stream()
                .filter(service -> !addedServiceIds.contains(service.getId()))
                .collect(Collectors.toList());

        // 5. Gửi danh sách đã lọc này ra view
        model.addAttribute("availableServices", availableServices);

        return "admin/booking-services";
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