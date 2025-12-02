package com.hotel.service;

import com.hotel.dto.RegisterDto;
import com.hotel.dto.UserDto;
import com.hotel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    void saveUser(RegisterDto registerDto);
    User findByUsername(String username);
    Page<User> findAllUsers(Pageable pageable);

    User findUserById(Long id);
    void adminSaveUser(UserDto userDto);
    void deleteUserById(Long id);
    void changePassword(String username, String oldPassword, String newPassword);
}