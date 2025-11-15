package com.hotel.service;

import com.hotel.dto.RoomDto;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // THÊM IMPORT NÀY
import java.util.List;
import java.time.LocalDate;

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
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    // ==========================================================
    // SỬA LỖI Ở HÀM SAVE (THÊM THAM SỐ MultipartFile)
    // ==========================================================
    @Override
    public void save(RoomDto dto, MultipartFile imageFile) { // <-- SỬA LỖI 1
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

        // room.setImageUrl(dto.getImageUrl()); // <-- XÓA DÒNG THỪA NÀY

        // Logic xử lý ảnh (giờ đã đúng vì imageFile đã tồn tại)
        if (imageFile != null && !imageFile.isEmpty()) { // <-- SỬA LỖI 2
            // Tải ảnh mới lên (Cloudinary)
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

    @Override
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            return new java.util.ArrayList<>();
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }
}