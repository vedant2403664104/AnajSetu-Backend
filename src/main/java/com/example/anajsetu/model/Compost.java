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
@Table(name = "compost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private FoodListing foodListing;

    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private User center;

    @Column(name = "redirected_at")
    private LocalDateTime redirectedAt = LocalDateTime.now();

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "biogas_output_kg")
    private Integer biogasOutputKg;

    @Column(nullable = false)
    private String status = "PENDING";

}