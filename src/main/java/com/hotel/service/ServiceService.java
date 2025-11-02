package com.hotel.service;

import com.hotel.dto.ServiceDto;
import com.hotel.entity.HotelService;
import java.util.List;

public interface ServiceService {
    List<HotelService> findAll();
    HotelService findById(Long id);
    void save(ServiceDto dto);
    void deleteById(Long id);
}