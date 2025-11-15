package com.hotel.repository;

import com.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
            "SELECT DISTINCT b.room.id FROM Booking b " +
            "WHERE b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOutDate " +
            "AND b.checkOutDate > :checkInDate" +
            ")")
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}