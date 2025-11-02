package com.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "services")
public class HotelService { // Đặt tên là HotelService để tránh nhầm lẫn
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private Double price;

    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    private Set<Booking> bookings = new HashSet<>();
}