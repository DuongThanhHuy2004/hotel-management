package com.hotel.repository;

import com.hotel.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm tất cả review của 1 phòng
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    // Tính điểm trung bình của 1 phòng
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.room.id = :roomId")
    Double getAverageRatingByRoomId(@Param("roomId") Long roomId);
}