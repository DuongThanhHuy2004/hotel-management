package com.hotel.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String password;
    private List<Long> roleIds;
}