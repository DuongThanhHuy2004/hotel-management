package com.hotel.controller;

import com.hotel.dto.ServiceDto;
import com.hotel.entity.HotelService;
import com.hotel.service.ServiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // R (Read) - List all
    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("services", serviceService.findAll());
        return "admin/services"; // (Sẽ tạo ở bước 8)
    }

    // C (Create) - Show form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("serviceDto", new ServiceDto());
        return "admin/service-form"; // (Sẽ tạo ở bước 8)
    }

    // U (Update) - Show form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        HotelService service = serviceService.findById(id);
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());

        model.addAttribute("serviceDto", dto);
        return "admin/service-form";
    }

    // C (Create) + U (Update) - Process form
    @PostMapping("/save")
    public String saveService(@ModelAttribute("serviceDto") ServiceDto dto) {
        serviceService.save(dto);
        return "redirect:/admin/services";
    }

    // D (Delete)
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        serviceService.deleteById(id);
        return "redirect:/admin/services";
    }
}