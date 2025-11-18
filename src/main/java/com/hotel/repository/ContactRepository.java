package com.hotel.repository;

import com.hotel.entity.Contact;
import org.springframework.data.domain.Page; // THÊM
import org.springframework.data.domain.Pageable; // THÊM
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    // SỬA HÀM NÀY (Từ List -> Page)
    Page<Contact> findAllByOrderBySentAtDesc(Pageable pageable);

    Long countByIsReadFalse(); // (Hàm này giữ nguyên)
}