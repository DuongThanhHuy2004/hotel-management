package com.hotel.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long bookingId;
    private Long roomId;
    private int rating;
    private String comment;
}