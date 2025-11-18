package com.hotel.service;

import com.hotel.dto.ContactDto;
import com.hotel.entity.Contact;
import com.hotel.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public void saveContact(ContactDto contactDto) {
        Contact contact = new Contact();
        contact.setFullName(contactDto.getFullName());
        contact.setEmail(contactDto.getEmail());
        contact.setSubject(contactDto.getSubject());
        contact.setMessage(contactDto.getMessage());
        contact.setRead(false); // Mặc định là chưa đọc

        contactRepository.save(contact);
    }

    @Override
    public Contact findById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact message not found"));
    }

    @Override
    public void markAsRead(Long id) {
        Contact contact = findById(id);
        contact.setRead(true);
        contactRepository.save(contact);
    }
    @Override
    public Page<Contact> findAll(Pageable pageable) {
        return contactRepository.findAllByOrderBySentAtDesc(pageable);
    }
}