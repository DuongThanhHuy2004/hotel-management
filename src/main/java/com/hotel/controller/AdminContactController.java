package com.hotel.controller;

import com.hotel.entity.Contact; // Thêm import
import com.hotel.service.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import com.hotel.entity.Contact;

@Controller
@RequestMapping("/admin/contacts")
public class AdminContactController {

    private final ContactService contactService;

    public AdminContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // 1. Hiển thị danh sách
    @GetMapping
    public String listContacts(Model model,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contact> contactPage = contactService.findAll(pageable);
        model.addAttribute("contactPage", contactPage); // Đổi tên biến
        return "admin/contacts";
    }

    // 2. (Optional) Xem chi tiết và đánh dấu đã đọc
    @GetMapping("/view/{id}")
    public String viewContact(@PathVariable Long id, Model model) {
        Contact contact = contactService.findById(id);
        if (!contact.isRead()) {
            contactService.markAsRead(id); // Đánh dấu đã đọc
        }
        model.addAttribute("contact", contact);
        return "admin/contact-view"; // (Sẽ tạo ở bước 9)
    }
}