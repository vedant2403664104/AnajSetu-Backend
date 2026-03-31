package com.example.anajsetu.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "donor_id") // Maps Java camelCase to SQL underscore
    private Integer donorId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer quantity;

    @Column(name = "quantity_unit")
    private String quantityUnit;

    @Column(name = "food_type")
    private String foodType; // THIS FIXES YOUR ERROR

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "pickup_deadline")
    private LocalDateTime pickupDeadline;

    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "claimed_by_id")
    private User claimedBy;
}