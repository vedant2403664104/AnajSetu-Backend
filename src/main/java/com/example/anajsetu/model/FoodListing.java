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

    @Column(name = "donor_id")
    private Integer donorId;

    @Column(name = "ngo_id")
    private Integer ngoId;

    @Column(name = "driver_id")
    private Integer driverId;

    private String title;

    @Column(name = "food_name")
    private String foodName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer quantity;           // keeps existing DB column

@Column(name = "quantity_str")
private String quantityStr;         // ← frontend sends here

    @Column(name = "quantity_unit")
    private String quantityUnit;

    @Column(name = "food_type")
    private String foodType;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "expiry_time")
private String expiryTime;          // ← changed LocalDateTime to String

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

    @Column(name = "claimed_by_name")
    private String claimedByName;

    @Column(name = "ngo_phone")
    private String ngoPhone;

    @Column(name = "donor_name")
    private String donorName;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }
}