package com.example.anajsetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Feedback entity representing user feedback within the AnajSetu platform.
 *
 * Feedback can be exchanged between any two users after a food donation interaction.
 * Examples:
 *  - NGO gives feedback to DONOR after receiving food.
 *  - DONOR gives feedback to NGO after a successful claim.
 *  - DELIVERY partner gives/receives feedback after completing a pickup.
 *
 * Key design decisions:
 *  - fromUserId / toUserId are Integer to match User.id (which is Integer in DB).
 *  - listingId is Integer (nullable) to match FoodListing.id; nullable because
 *    some feedback scenarios may not be tied to a specific listing.
 *  - feedbackType is a free-form string (e.g., "DONATION", "CLAIM", "DELIVERY")
 *    to keep the system generic and role-agnostic.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * References food_listings.id. Nullable — some feedback may not be
     * tied to a specific listing (e.g., general platform feedback).
     */
    @Column(name = "listing_id", nullable = true)
    private Integer listingId;

    /** ID of the user who is giving the feedback. Maps to users.id (Integer). */
    @Column(name = "from_user_id", nullable = false)
    private Integer fromUserId;

    /** Role of the giver at the time of feedback (e.g. "DONOR", "NGO", "DELIVERY"). */
    @Column(name = "from_user_role", nullable = false)
    private String fromUserRole;

    /** ID of the user who is receiving the feedback. Maps to users.id (Integer). */
    @Column(name = "to_user_id", nullable = false)
    private Integer toUserId;

    /** Role of the receiver at the time of feedback. */
    @Column(name = "to_user_role", nullable = false)
    private String toUserRole;

    /** Rating from 1 (poor) to 5 (excellent). */
    @Column(nullable = false)
    private Integer rating;

    /** Optional comment/review text. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Category of the feedback interaction.
     * Suggested values: "DONATION", "CLAIM", "DELIVERY", "GENERAL".
     * Kept as String for flexibility.
     */
    @Column(name = "feedback_type", nullable = false)
    private String feedbackType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Feedback() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getListingId() { return listingId; }
    public void setListingId(Integer listingId) { this.listingId = listingId; }

    public Integer getFromUserId() { return fromUserId; }
    public void setFromUserId(Integer fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserRole() { return fromUserRole; }
    public void setFromUserRole(String fromUserRole) { this.fromUserRole = fromUserRole; }

    public Integer getToUserId() { return toUserId; }
    public void setToUserId(Integer toUserId) { this.toUserId = toUserId; }

    public String getToUserRole() { return toUserRole; }
    public void setToUserRole(String toUserRole) { this.toUserRole = toUserRole; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}