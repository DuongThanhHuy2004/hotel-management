package com.hotel.service;

import com.hotel.dto.ServiceDto;
import com.hotel.entity.HotelService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {
    List<HotelService> findAll();
    HotelService findById(Long id);
    void save(ServiceDto dto);
    void deleteById(Long id);
    Page<HotelService> findAll(Pageable pageable);
}