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

// Import cho Phân trang (Sprint 14)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final FileStorageService fileStorageService; // Cho Sprint 9 (Upload)

    public RoomServiceImpl(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, FileStorageService fileStorageService) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Hàm này (trả về Page)
     * Dùng cho Admin xem danh sách phòng (đã phân trang)
     */
    @Override
    public Page<Room> findAll(Pageable pageable) {
        return roomRepository.findAllByOrderByIdDesc(pageable);
    }

    @Override
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    /**
     * Hàm này cho Sprint 9 (Upload ảnh)
     */
    @Override
    public void save(RoomDto dto, MultipartFile imageFile) {
        Room room;
        if (dto.getId() != null) {
            room = findById(dto.getId());
        } else {
            room = new Room();
        }

        RoomType roomType = roomTypeRepository.findById(dto.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found for ID: " + dto.getRoomTypeId()));

        room.setRoomNumber(dto.getRoomNumber());
        room.setPrice(dto.getPrice());
        room.setDescription(dto.getDescription());
        room.setAvailable(dto.isAvailable());
        room.setRoomType(roomType);

        // Logic xử lý ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(imageFile);
            room.setImageUrl(imageUrl);
        } else if (dto.getId() == null) {
            // Gán 1 ảnh mặc định nếu tạo mới mà không up ảnh
            room.setImageUrl("/sona/img/room/room-default.jpg");
        }

        roomRepository.save(room);
    }

    @Override
    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }

    /**
     * Hàm này (trả về Page)
     * Dùng cho Client tìm phòng (đã phân trang)
     */
    @Override
    public Page<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Pageable pageable) {
        if (checkIn.isAfter(checkOut)) {
            return Page.empty(pageable); // Trả về trang rỗng
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut, pageable);
    }

    /**
     * Hàm này (trả về List)
     * Dùng cho trang chủ Sona (hiển thị 3 phòng)
     */
    @Override
    public List<Room> findTop3ByOrderByIdDesc() {
        return roomRepository.findTop3ByOrderByIdDesc();
    }
}