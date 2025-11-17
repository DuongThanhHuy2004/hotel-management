package com.hotel.service;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    List<Room> findAll();
    Room findById(Long id);
    void save(RoomDto dto, MultipartFile imageFile);
    void deleteById(Long id);

    List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut);
    List<Room> findTop3ByOrderByIdDesc();
}