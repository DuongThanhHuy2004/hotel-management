package com.hotel.service;

import com.hotel.dto.ContactDto;
import com.hotel.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    void saveContact(ContactDto contactDto);
    Contact findById(Long id);
    void markAsRead(Long id);
    Page<Contact> findAll(Pageable pageable);
}