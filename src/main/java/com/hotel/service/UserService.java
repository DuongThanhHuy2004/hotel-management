package com.hotel.service;

import com.hotel.dto.RegisterDto;
import com.hotel.dto.UserDto;
import com.hotel.entity.User;
import java.util.List;

public interface UserService {
    void saveUser(RegisterDto registerDto);
    User findByUsername(String username);

    List<User> findAllUsers();
    User findUserById(Long id);
    void adminSaveUser(UserDto userDto); // Dùng cho cả Create và Update
    void deleteUserById(Long id);
    void changePassword(String username, String oldPassword, String newPassword);
    void processOAuthPostLogin(String email, String fullName, User.Provider provider, String providerId);
}