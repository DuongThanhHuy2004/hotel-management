package com.hotel.repository;

import com.hotel.entity.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceRepository extends JpaRepository<HotelService, Long> {
    Page<HotelService> findAllByOrderByIdDesc(Pageable pageable);
}