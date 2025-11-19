package com.hotel.service;

import com.hotel.dto.ReviewDto;
import com.hotel.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReviewService {
    void saveReview(ReviewDto reviewDto, String username);
    List<Review> getReviewsForRoom(Long roomId);
    double getAverageRatingForRoom(Long roomId);
    void deleteReview(Long id);
    Page<Review> findAll(Pageable pageable);
    List<Review> findTop3ByOrderByCreatedAtDesc();
}