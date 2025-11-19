package com.hotel.controller;

import com.hotel.repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final BookingRepository bookingRepository;

    public DashboardApiController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // API cho biểu đồ "Site Traffic" (Doanh thu theo tháng)
    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue(
            @RequestParam(name = "year", required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<Map<String, Object>> data = bookingRepository.getMonthlyRevenue(targetYear);
        return ResponseEntity.ok(data);
    }

    // API cho biểu đồ "Weekly Sales" (Tỷ lệ đặt phòng)
    @GetMapping("/room-type-popularity")
    public ResponseEntity<List<Map<String, Object>>> getRoomTypePopularity() {
        List<Map<String, Object>> data = bookingRepository.getRoomTypeBookingCounts();
        return ResponseEntity.ok(data);
    }
}