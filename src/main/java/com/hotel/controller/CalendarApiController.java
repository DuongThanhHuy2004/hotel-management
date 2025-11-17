package com.hotel.controller;

import com.hotel.dto.BookingCalendarDto;
import com.hotel.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarApiController {

    private final BookingService bookingService;

    public CalendarApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingCalendarDto>> getAllBookings() {
        List<BookingCalendarDto> bookings = bookingService.getCalendarBookings();
        return ResponseEntity.ok(bookings);
    }
}