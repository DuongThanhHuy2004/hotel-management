package com.hotel.service;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import java.util.List;

public interface RoomService {
    List<Room> findAll();
    Room findById(Long id);
    void save(RoomDto dto);
    void deleteById(Long id);
}