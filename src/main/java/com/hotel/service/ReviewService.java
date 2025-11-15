package com.hotel.service;

import com.hotel.dto.ReviewDto;
import com.hotel.entity.Review;
import java.util.List;

public interface ReviewService {
    void saveReview(ReviewDto reviewDto, String username);
    List<Review> getReviewsForRoom(Long roomId);
    double getAverageRatingForRoom(Long roomId);
    List<Review> findAll();
    void deleteReview(Long id);
}