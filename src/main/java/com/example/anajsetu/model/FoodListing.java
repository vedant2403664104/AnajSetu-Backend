package com.example.anajsetu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_unit", nullable = false)
    private String quantityUnit;

    @Column(name = "food_type", nullable = false)
    private String foodType;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "pickup_deadline", nullable = false)
    private LocalDateTime pickupDeadline;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}