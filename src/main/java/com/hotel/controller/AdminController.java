package com.hotel.controller;

import com.hotel.dto.UserDto;
import com.hotel.entity.Role;
import com.hotel.entity.User;
import com.hotel.repository.RoleRepository;
import com.hotel.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hotel.entity.User;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    // R (Read) - List all users
    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.findAllUsers(pageable);
        model.addAttribute("userPage", userPage); // Đổi tên biến
        return "admin/users";
    }
    // C (Create) - Show form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/user-form"; // (Sẽ tạo ở bước 7)
    }

    // U (Update) - Show form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id);
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFullName(user.getFullName());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setRoleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));

        model.addAttribute("userDto", userDto);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/user-form";
    }

    // C (Create) + U (Update) - Process form
    @PostMapping("/save")
    public String saveUser(@ModelAttribute("userDto") UserDto userDto) {
        userService.adminSaveUser(userDto);
        return "redirect:/admin/users";
    }

    // D (Delete)
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }
}