package com.hotel.service;

import com.hotel.dto.ReviewDto;
import com.hotel.entity.Booking;
import com.hotel.entity.Review;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void saveReview(ReviewDto reviewDto, String username) {
        Booking booking = bookingRepository.findById(reviewDto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 1. Kiểm tra bảo mật: Đúng người dùng
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You cannot review a booking that is not yours.");
        }
        // 2. Kiểm tra nghiệp vụ: Đã ở xong
        if (!"CONFIRMED".equals(booking.getStatus()) || booking.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("You can only review completed bookings.");
        }
        // 3. Kiểm tra nghiệp vụ: Chưa review
        if (booking.isHasReviewed()) {
            throw new RuntimeException("You have already reviewed this booking.");
        }

        // Tạo review
        Review review = new Review();
        review.setBooking(booking);
        review.setUser(booking.getUser());
        review.setRoom(booking.getRoom());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        reviewRepository.save(review);

        // Đánh dấu booking này đã được review
        booking.setHasReviewed(true);
        bookingRepository.save(booking);
    }

    @Override
    public List<Review> getReviewsForRoom(Long roomId) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }

    @Override
    public double getAverageRatingForRoom(Long roomId) {
        return reviewRepository.getAverageRatingByRoomId(roomId);
    }

    @Override
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Override
    public void deleteReview(Long id) {
        // (Nâng cao: Cần xóa review và set hasReviewed = false cho booking)
        Review review = reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        Booking booking = review.getBooking();
        booking.setHasReviewed(false);
        bookingRepository.save(booking);
        reviewRepository.deleteById(id);
    }

    @Override
    public List<Review> findTop3ByOrderByCreatedAtDesc() {
        return reviewRepository.findTop3ByOrderByCreatedAtDesc();
    }
}