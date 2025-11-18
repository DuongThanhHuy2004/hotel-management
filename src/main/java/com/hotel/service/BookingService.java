package com.hotel.service;

import com.hotel.dto.BookingDto;
import com.hotel.entity.Booking;
import java.util.List;
import java.time.LocalDate;
import com.hotel.dto.BookingCalendarDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    // Client
    Booking createBooking(BookingDto bookingDto, String username);
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    // Admin
    Booking findById(Long id);
    void confirmBooking(Long id);
    void cancelBooking(Long id);

    void addServiceToBooking(Long bookingId, Long serviceId);
    void removeServiceFromBooking(Long bookingId, Long serviceId);
    void cancelMyBooking(Long bookingId, String username);

    List<BookingCalendarDto> getCalendarBookings();

    Page<Booking> findAll(Pageable pageable);
    Page<Booking> findBookingsByUsername(String username, Pageable pageable);
}