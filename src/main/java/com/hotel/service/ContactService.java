package com.hotel.service;

import com.hotel.dto.ContactDto;
import com.hotel.entity.Contact;
import org.springframework.data.domain.Page; // THÊM
import org.springframework.data.domain.Pageable; // THÊM
import java.util.List; // (Xóa import này nếu không dùng List nữa)

public interface ContactService {
    void saveContact(ContactDto contactDto);
    Contact findById(Long id);
    void markAsRead(Long id);

    // ===================================
    // SỬA LỖI Ở DÒNG NÀY (THÊM Pageable)
    // ===================================
    Page<Contact> findAll(Pageable pageable);
}