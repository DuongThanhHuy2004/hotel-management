package com.hotel.service;

import com.hotel.dto.RegisterDto;
import com.hotel.dto.UserDto;
import com.hotel.entity.Role;
import com.hotel.entity.User;
import com.hotel.repository.RoleRepository;
import com.hotel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setFullName(registerDto.getFullName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public void adminSaveUser(UserDto userDto) {
        User user;
        if (userDto.getId() != null) {
            // Cập nhật (Update)
            user = findUserById(userDto.getId());
        } else {
            // Tạo mới (Create)
            user = new User();
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("Username is already taken!");
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Email is already taken!");
            }
        }

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());

        // Chỉ cập nhật password nếu nó được cung cấp (cho cả create và update)
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // Cập nhật Roles
        if (userDto.getRoleIds() != null && !userDto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(userDto.getRoleIds()));
            user.setRoles(roles);
        } else {
            // Mặc định gán ROLE_USER nếu không có role nào được chọn
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            user.setRoles(Set.of(userRole));
        }

        userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id) {
        // (Thêm kiểm tra an toàn, ví dụ: không cho xóa user 'admin')
        User user = findUserById(id);
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("Cannot delete admin user!");
        }

        // Xóa các liên kết trong bảng users_roles trước
        user.setRoles(null);
        userRepository.save(user);

        userRepository.deleteById(id);
    }
}