package com.hotel.repository;

import com.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query(value = "SELECT r FROM Room r WHERE r.id NOT IN (" +
            "SELECT DISTINCT b.room.id FROM Booking b " +
            "WHERE b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOutDate " +
            "AND b.checkOutDate > :checkInDate" +
            ")",
            countQuery = "SELECT count(r) FROM Room r WHERE r.id NOT IN (" +
                    "SELECT DISTINCT b.room.id FROM Booking b " +
                    "WHERE b.status IN ('PENDING', 'CONFIRMED') " +
                    "AND b.checkInDate < :checkOutDate " +
                    "AND b.checkOutDate > :checkInDate" +
                    ")")
    Page<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            Pageable pageable
    );
    Page<Room> findAllByOrderByIdDesc(Pageable pageable);
    List<Room> findTop4ByOrderByIdDesc();
}