package com.hotel.controller;

import com.hotel.dto.RoomTypeDto;
import com.hotel.entity.RoomType;
import com.hotel.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/room-types")
public class AdminRoomTypeController {

    private final RoomTypeService roomTypeService;

    public AdminRoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String listRoomTypes(Model model,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomType> roomTypePage = roomTypeService.findAll(pageable);
        model.addAttribute("roomTypePage", roomTypePage);
        return "admin/room-types";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("roomTypeDto", new RoomTypeDto());
        return "admin/room-type-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoomType roomType = roomTypeService.findById(id);
        RoomTypeDto dto = new RoomTypeDto();
        dto.setId(roomType.getId());
        dto.setName(roomType.getName());
        dto.setDescription(roomType.getDescription());
        model.addAttribute("roomTypeDto", dto);
        return "admin/room-type-form";
    }

    @PostMapping("/save")
    public String saveRoomType(@ModelAttribute("roomTypeDto") RoomTypeDto dto) {
        roomTypeService.save(dto);
        return "redirect:/admin/room-types";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteById(id);
        return "redirect:/admin/room-types";
    }
}