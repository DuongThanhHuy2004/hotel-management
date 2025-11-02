package com.hotel.dto;

import lombok.Data;

@Data
public class ServiceDto {
    private Long id;
    private String name;
    private String description;
    private Double price;
}