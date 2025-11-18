package com.hotel.service;

import com.hotel.dto.RoomTypeDto;
import com.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RoomTypeService {
    List<RoomType> findAll();
    RoomType findById(Long id);
    void save(RoomTypeDto dto);
    void deleteById(Long id);
    Page<RoomType> findAll(Pageable pageable);
}