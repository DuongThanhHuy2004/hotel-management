package com.hotel.service;

import com.hotel.dto.ReviewDto;
import com.hotel.entity.Review;
import org.springframework.data.domain.Page; // THÊM IMPORT NÀY
import org.springframework.data.domain.Pageable; // THÊM IMPORT NÀY
import java.util.List;

public interface ReviewService {

    // (Các hàm cũ saveReview, getReviewsForRoom... giữ nguyên)
    void saveReview(ReviewDto reviewDto, String username);
    List<Review> getReviewsForRoom(Long roomId);
    double getAverageRatingForRoom(Long roomId);
    void deleteReview(Long id);

    // ===================================
    // SỬA LỖI Ở DÒNG NÀY (THÊM Pageable)
    // ===================================
    Page<Review> findAll(Pageable pageable);

    // (Hàm này cho trang chủ, giữ nguyên)
    List<Review> findTop3ByOrderByCreatedAtDesc();
}