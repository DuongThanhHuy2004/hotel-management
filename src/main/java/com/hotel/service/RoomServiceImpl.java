package com.hotel.service;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final FileStorageService fileStorageService;

    public RoomServiceImpl(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, FileStorageService fileStorageService) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.fileStorageService = fileStorageService;
    }
    @Override
    public Page<Room> findAll(Pageable pageable) {
        return roomRepository.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
    }
    @Override
    public void save(RoomDto dto, MultipartFile imageFile) {
        Room room;
        if (dto.getId() != null) {
            room = findById(dto.getId());
        } else {
            room = new Room();
        }

        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Không thể tìm thấy phòng với ID: " + dto.getRoomTypeId()));

        room.setRoomNumber(dto.getRoomNumber());
        room.setPrice(dto.getPrice());
        room.setDescription(dto.getDescription());
        room.setAvailable(dto.isAvailable());
        room.setRoomType(roomType);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(imageFile);
            room.setImageUrl(imageUrl);
        } else if (dto.getId() == null) {
            room.setImageUrl("/sona/img/room/room-default.jpg");
        }

        roomRepository.save(room);
    }

    @Override
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public Page<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Pageable pageable) {
        if (checkIn.isAfter(checkOut)) {
            return Page.empty(pageable);
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut, pageable);
    }
    @Override
    public List<Room> findTop4ByOrderByIdDesc() {
        return roomRepository.findTop4ByOrderByIdDesc();
    }
}