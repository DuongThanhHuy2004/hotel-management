package com.hotel.repository;

import com.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Tìm các đơn đặt phòng (Đã xác nhận hoặc Đang chờ)
     * của một phòng cụ thể mà có ngày bị trùng lặp với khoảng ngày mới.
     * * Logic: (Ngày Check-in mới < Ngày Check-out cũ) VÀ (Ngày Check-out mới > Ngày Check-in cũ)
     */
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOutDate " +
            "AND b.checkOutDate > :checkInDate")
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    List<Booking> findByUserUsernameOrderByBookingDateDesc(String username);
}