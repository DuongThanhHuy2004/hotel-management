package com.hotel.dto;

import lombok.Data;

@Data
public class ContactDto {
    private String fullName;
    private String email;
    private String subject;
    private String message;
}