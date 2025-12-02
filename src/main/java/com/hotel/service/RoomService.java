package com.hotel.service;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    Page<Room> findAll(Pageable pageable);
    Page<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Pageable pageable);
    Room findById(Long id);
    void save(RoomDto dto, MultipartFile imageFile);
    void deleteById(Long id);
    List<Room> findTop4ByOrderByIdDesc();
}