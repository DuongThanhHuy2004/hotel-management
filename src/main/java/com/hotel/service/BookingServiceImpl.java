package com.hotel.service;

import com.hotel.dto.BookingDto;
import com.hotel.entity.Booking;
import com.hotel.entity.Room;
import com.hotel.entity.User;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.hotel.entity.HotelService;
import com.hotel.repository.ServiceRepository;
import java.util.HashSet;
import java.util.Set;

@Service
public
class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, RoomRepository roomRepository, ServiceRepository serviceRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public Booking createBooking(BookingDto bookingDto, String username) {
        // 1. Kiểm tra ngày hợp lệ
        if (bookingDto.getCheckInDate().isAfter(bookingDto.getCheckOutDate()) ||
                bookingDto.getCheckInDate().isEqual(bookingDto.getCheckOutDate())) {
            throw new RuntimeException("Check-out date must be after check-in date.");
        }
        if (bookingDto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past.");
        }

        // 2. Kiểm tra phòng có trống không
        if (!isRoomAvailable(bookingDto.getRoomId(), bookingDto.getCheckInDate(), bookingDto.getCheckOutDate())) {
            throw new RuntimeException("Room is not available for the selected dates.");
        }

        // 3. Lấy thông tin User và Room
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Room room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 4. (MỚI) Lấy danh sách dịch vụ
        Set<HotelService> services = new HashSet<>();
        if (bookingDto.getServiceIds() != null && !bookingDto.getServiceIds().isEmpty()) {
            services.addAll(serviceRepository.findAllById(bookingDto.getServiceIds()));
        }

        // 5. (MỚI) Tính toán lại tổng tiền
        long numberOfNights = ChronoUnit.DAYS.between(bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        double roomPrice = room.getPrice() * numberOfNights;
        double servicesPrice = services.stream().mapToDouble(HotelService::getPrice).sum();
        double totalPrice = roomPrice + servicesPrice;

        // 6. Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDto.getCheckInDate());
        booking.setCheckOutDate(bookingDto.getCheckOutDate());
        booking.setTotalPrice(totalPrice); // Dùng tổng tiền mới
        booking.setStatus("PENDING");
        booking.setServices(services); // Gán dịch vụ vào đơn

        return bookingRepository.save(booking);
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(roomId, checkInDate, checkOutDate);
        return conflicts.isEmpty(); // Nếu list rỗng, tức là phòng available
    }

    @Override
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Override
    public void confirmBooking(Long id) {
        Booking booking = findById(id);
        // (Có thể thêm logic kiểm tra xem phòng có bị conflict với đơn ĐÃ CONFIRM khác không)
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
    }

    @Override
    public void cancelBooking(Long id) {
        Booking booking = findById(id);
        booking.setStatus("CANCELED");
        bookingRepository.save(booking);
    }

    public List<Booking> findBookingsByUsername(String username) {
        return bookingRepository.findByUserUsernameOrderByBookingDateDesc(username);
    }

    private void recalculateTotalPrice(Booking booking) {
        // Lấy giá phòng gốc
        long numberOfNights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        double roomPrice = booking.getRoom().getPrice() * numberOfNights;

        // Cộng thêm giá của tất cả dịch vụ
        double servicesPrice = booking.getServices().stream()
                .mapToDouble(HotelService::getPrice)
                .sum();

        booking.setTotalPrice(roomPrice + servicesPrice);
    }

    @Override
    public void addServiceToBooking(Long bookingId, Long serviceId) {
        Booking booking = findById(bookingId);
        HotelService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Thêm dịch vụ vào đơn
        booking.getServices().add(service);

        // Tính lại tổng tiền
        recalculateTotalPrice(booking);

        // Lưu lại
        bookingRepository.save(booking);
    }

    @Override
    public void removeServiceFromBooking(Long bookingId, Long serviceId) {
        Booking booking = findById(bookingId);
        HotelService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Xóa dịch vụ khỏi đơn
        booking.getServices().remove(service);

        // Tính lại tổng tiền
        recalculateTotalPrice(booking);

        // Lưu lại
        bookingRepository.save(booking);
    }
}