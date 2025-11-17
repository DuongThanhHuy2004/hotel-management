package com.hotel.dto;

import lombok.Data;

@Data
public class BookingCalendarDto {
    private String title; // (Room 101 - client_user)
    private String start; // (2025-11-10)
    private String end;   // (2025-11-12)
    private String color; // (Màu xanh cho CONFIRMED, vàng cho PENDING)
}