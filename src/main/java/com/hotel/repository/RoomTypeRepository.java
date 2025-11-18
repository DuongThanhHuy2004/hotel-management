package com.hotel.repository;

import com.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    Page<RoomType> findAllByOrderByIdDesc(Pageable pageable);
}