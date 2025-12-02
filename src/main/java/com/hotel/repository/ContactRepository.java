package com.hotel.repository;

import com.hotel.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findAllByOrderBySentAtDesc(Pageable pageable);
    Long countByIsReadFalse();
}