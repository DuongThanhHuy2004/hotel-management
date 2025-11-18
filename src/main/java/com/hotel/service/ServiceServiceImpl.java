package com.hotel.service;

import com.hotel.dto.ServiceDto;
import com.hotel.entity.HotelService;
import com.hotel.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service // Chú ý đây là @Service của Spring
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public List<HotelService> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public HotelService findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }

    @Override
    public void save(ServiceDto dto) {
        HotelService service;
        if (dto.getId() != null) {
            service = findById(dto.getId());
        } else {
            service = new HotelService();
        }
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());

        serviceRepository.save(service);
    }

    @Override
    public void deleteById(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public Page<HotelService> findAll(Pageable pageable) {
        return serviceRepository.findAllByOrderByIdDesc(pageable);
    }
}