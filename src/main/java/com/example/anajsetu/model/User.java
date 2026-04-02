package com.example.anajsetu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;           // ← now nullable (optional for phone users)

    private String password;        // ← now nullable (optional for phone users)

    @Column(unique = true)
    private String phone;           // ← now unique (main login identifier)

    @Column(nullable = false)
    private String role;            // "USER", "NGO", "VOLUNTEER"

    @Column(name = "is_verified")
    private Integer isVerified = 0; // 0 = not verified, 1 = verified

    private String address;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}