package com.hotel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import jakarta.persistence.CascadeType;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Sẽ dùng email cho cả username và email

    @Column(unique = true) // Sửa: Cho phép email null tạm thời (dù ta sẽ luôn set)
    private String email;

    @Column(nullable = true) // <-- SỬA: Cho phép password là null
    private String password;

    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles;

    // --- THÊM CÁC TRƯỜNG MỚI ---
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider; // (LOCAL, GOOGLE, FACEBOOK)

    private String providerId; // ID của user đó trên Google

    // Thêm Enum Provider
    public enum Provider {
        LOCAL, GOOGLE, FACEBOOK
    }
}