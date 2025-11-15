package com.hotel.controller;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import com.hotel.service.RoomTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;

    public AdminRoomController(RoomService roomService, RoomTypeService roomTypeService) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "admin/rooms";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("roomDto", new RoomDto());
        model.addAttribute("allRoomTypes", roomTypeService.findAll());
        return "admin/room-form"; // (Sẽ tạo view ở bước 8)
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomService.findById(id);
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setPrice(room.getPrice());
        dto.setDescription(room.getDescription());
        dto.setImageUrl(room.getImageUrl());
        dto.setAvailable(room.isAvailable());
        dto.setRoomTypeId(room.getRoomType().getId());

        model.addAttribute("roomDto", dto);
        model.addAttribute("allRoomTypes", roomTypeService.findAll());
        return "admin/room-form";
    }

    @PostMapping("/save")
    public String saveRoom(@ModelAttribute("roomDto") RoomDto dto,
                           @RequestParam("imageFile") MultipartFile imageFile) { // <-- THÊM MỚI

        roomService.save(dto, imageFile); // <-- SỬA LẠI

        return "redirect:/admin/rooms";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        return "redirect:/admin/rooms";
    }
}