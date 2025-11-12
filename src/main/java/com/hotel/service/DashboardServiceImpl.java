package com.hotel.service;

import com.hotel.repository.BookingRepository;
import com.hotel.repository.ContactRepository;
import com.hotel.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public DashboardServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ContactRepository contactRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Thẻ 1: Total Orders (Chỉ đếm đơn PENDING)
        summary.put("totalOrders", bookingRepository.countByStatus("PENDING"));

        // Thẻ 2: Total Revenue (Chỉ tính đơn CONFIRMED)
        summary.put("totalRevenue", bookingRepository.getConfirmedTotalRevenue());

        // Thẻ 3: "Visitors" -> Đổi thành "Total Users"
        summary.put("totalUsers", userRepository.count());

        // Thẻ 4: "Messages" -> Đổi thành "Unread Messages"
        summary.put("unreadMessages", contactRepository.countByIsReadFalse());

        return summary;
    }
}