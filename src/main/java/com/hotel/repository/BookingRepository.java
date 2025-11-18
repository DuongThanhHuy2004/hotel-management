package com.hotel.repository;

import com.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // 1. Lấy tổng doanh thu của các đơn đã CONFIRMED
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double getConfirmedTotalRevenue();

    // 2. Đếm số đơn theo trạng thái
    Long countByStatus(String status);

    // 3. Lấy doanh thu theo từng tháng (cho biểu đồ line)
    @Query("SELECT MONTH(b.checkInDate) as month, SUM(b.totalPrice) as revenue " +
            "FROM Booking b WHERE b.status = 'CONFIRMED' AND YEAR(b.checkInDate) = :year " +
            "GROUP BY MONTH(b.checkInDate) " +
            "ORDER BY MONTH(b.checkInDate) ASC") // <-- SỬA LẠI DÒNG NÀY (lặp lại hàm)
    List<Map<String, Object>> getMonthlyRevenue(@Param("year") int year);

    // 4. Lấy số lượng đặt của từng loại phòng (cho biểu đồ tròn)
    @Query("SELECT b.room.roomType.name as roomType, COUNT(b) as count " +
            "FROM Booking b WHERE b.status = 'CONFIRMED' " +
            "GROUP BY b.room.roomType.name")
    List<Map<String, Object>> getRoomTypeBookingCounts();

    Page<Booking> findByUserUsernameOrderByBookingDateDesc(String username, Pageable pageable);
    Page<Booking> findAllByOrderByIdDesc(Pageable pageable);
    List<Booking> findByStatusIn(Collection<String> statuses);
}