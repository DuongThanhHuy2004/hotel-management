package com.hotel.dto;

import lombok.Data;

@Data
public class RoomDto {
    private Long id;
    private String roomNumber;
    private Double price;
    private String description;
    private String imageUrl;
    private boolean isAvailable;
    private Long roomTypeId;
}