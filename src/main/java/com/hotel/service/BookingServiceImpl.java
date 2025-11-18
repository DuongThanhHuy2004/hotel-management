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
import org.springframework.security.access.AccessDeniedException;
import com.hotel.entity.Payment;
import com.hotel.repository.PaymentRepository;
import com.hotel.dto.BookingCalendarDto;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public
class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final PaymentRepository paymentRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, RoomRepository roomRepository, ServiceRepository serviceRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.paymentRepository = paymentRepository;
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
    public Page<Booking> findAll(Pageable pageable) {
        return bookingRepository.findAllByOrderByIdDesc(pageable);
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

    public Page<Booking> findBookingsByUsername(String username,  Pageable pageable) {
        return bookingRepository.findByUserUsernameOrderByBookingDateDesc(username, pageable);
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

    @Override
    public void cancelMyBooking(Long bookingId, String username) {
        Booking booking = findById(bookingId); // Dùng hàm findById có sẵn

        // 1. Kiểm tra xem đơn này có phải của user này không
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to cancel this booking.");
        }

        // 2. Kiểm tra xem đơn có đang PENDING không
        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be canceled.");
        }

        // 3. Hủy đơn (dùng lại logic cũ)
        booking.setStatus("CANCELED");
        bookingRepository.save(booking);

        // (Ghi lại nhật ký thanh toán thất bại nếu muốn)
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setTransactionCode("CLIENT_CANCELED");
        payment.setPaymentMethod("N/A");
        payment.setStatus("CANCELED");
        paymentRepository.save(payment);
    }

    @Override
    public List<BookingCalendarDto> getCalendarBookings() {
        // 1. Lấy tất cả các đơn đang PENDING hoặc CONFIRMED
        List<Booking> bookings = bookingRepository.findByStatusIn(List.of("PENDING", "CONFIRMED"));

        // 2. Chuyển đổi (map) chúng sang DTO mà Calendar hiểu
        return bookings.stream()
                .map(this::mapToCalendarDto)
                .collect(Collectors.toList());
    }

    // Hàm private hỗ trợ chuyển đổi
    private BookingCalendarDto mapToCalendarDto(Booking booking) {
        BookingCalendarDto dto = new BookingCalendarDto();

        // Tiêu đề: "Room 101 - client"
        dto.setTitle("Room " + booking.getRoom().getRoomNumber() + " - " + booking.getUser().getUsername());

        // Ngày bắt đầu
        dto.setStart(booking.getCheckInDate().toString()); // (yyyy-MM-dd)

        // Ngày kết thúc (FullCalendar coi ngày kết thúc là độc quyền (exclusive))
        // Vì vậy, check-out ngày 12 thì event sẽ kết thúc vào đầu ngày 12 (hiển thị đến hết ngày 11).
        // Đây chính là logic đúng của chúng ta.
        dto.setEnd(booking.getCheckOutDate().toString());

        // Đặt màu dựa trên trạng thái
        if ("CONFIRMED".equals(booking.getStatus())) {
            dto.setColor("#28a745"); // Màu xanh lá
        } else {
            dto.setColor("#ffc107"); // Màu vàng (Pending)
        }

        return dto;
    }
}