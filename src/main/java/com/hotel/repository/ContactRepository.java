package com.hotel.repository;

import com.hotel.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Tự động sắp xếp tin nhắn mới nhất lên đầu
    List<Contact> findAllByOrderBySentAtDesc();
    Long countByIsReadFalse();
}