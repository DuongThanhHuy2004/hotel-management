package com.hotel.service;

import com.hotel.dto.ContactDto;
import com.hotel.entity.Contact;
import java.util.List;

public interface ContactService {
    void saveContact(ContactDto contactDto);
    List<Contact> findAll();
    Contact findById(Long id);
    void markAsRead(Long id);
}