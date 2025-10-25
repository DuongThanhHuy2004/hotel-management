package com.hotel.service;

import com.hotel.dto.RoomTypeDto;
import com.hotel.entity.RoomType;
import java.util.List;

public interface RoomTypeService {
    List<RoomType> findAll();
    RoomType findById(Long id);
    void save(RoomTypeDto dto);
    void deleteById(Long id);
}