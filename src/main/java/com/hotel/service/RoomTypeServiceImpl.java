package com.hotel.service;

import com.hotel.dto.RoomTypeDto;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

// Import cho Phân trang (Sprint 14)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Override
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    @Override
    public Page<RoomType> findAll(Pageable pageable) {
        return roomTypeRepository.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public RoomType findById(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));
    }

    @Override
    public void save(RoomTypeDto dto) {
        RoomType roomType;
        if (dto.getId() != null) {
            roomType = findById(dto.getId());
        } else {
            roomType = new RoomType();
        }
        roomType.setName(dto.getName());
        roomType.setDescription(dto.getDescription());
        roomTypeRepository.save(roomType);
    }

    @Override
    public void deleteById(Long id) {
        roomTypeRepository.deleteById(id);
    }
}