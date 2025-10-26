package com.hotel.service;

import com.hotel.dto.BookingDto;
import com.hotel.entity.Booking;
import java.util.List;
import java.time.LocalDate;

public interface BookingService {
    // Client
    Booking createBooking(BookingDto bookingDto, String username);
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    // Admin
    List<Booking> findAll();
    Booking findById(Long id);
    void confirmBooking(Long id);
    void cancelBooking(Long id);

    List<Booking> findBookingsByUsername(String username);
}