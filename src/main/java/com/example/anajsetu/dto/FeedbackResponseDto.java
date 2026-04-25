package com.example.anajsetu.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for Feedback endpoints.
 *
 * Extends the raw Feedback entity data with resolved sender identity fields
 * (fromUserName) so the frontend can display "who sent the feedback" without a
 * second API call.
 *
 * Fields deliberately omitted: password, isVerified, internal User timestamps,
 * address, latitude, longitude — none of which are relevant to the receiver.
 */
public class FeedbackResponseDto {

    private Long    id;
    private Integer listingId;
    private Integer rating;
    private String  comment;
    private LocalDateTime createdAt;
    private String  feedbackType;

    // Sender identity (from Feedback entity)
    private Integer fromUserId;
    private String  fromUserRole;

    // Sender display info (resolved from User entity)
    private String  fromUserName;

    // Receiver identity
    private Integer toUserId;
    private String  toUserRole;

    // ── Constructor ───────────────────────────────────────────────────────────

    public FeedbackResponseDto(
            Long id,
            Integer listingId,
            Integer rating,
            String comment,
            LocalDateTime createdAt,
            String feedbackType,
            Integer fromUserId,
            String fromUserRole,
            String fromUserName,
            Integer toUserId,
            String toUserRole) {

        this.id           = id;
        this.listingId    = listingId;
        this.rating       = rating;
        this.comment      = comment;
        this.createdAt    = createdAt;
        this.feedbackType = feedbackType;
        this.fromUserId   = fromUserId;
        this.fromUserRole = fromUserRole;
        this.fromUserName = fromUserName;
        this.toUserId     = toUserId;
        this.toUserRole   = toUserRole;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public Integer getListingId()        { return listingId; }
    public Integer getRating()           { return rating; }
    public String getComment()           { return comment; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public String getFeedbackType()      { return feedbackType; }
    public Integer getFromUserId()       { return fromUserId; }
    public String getFromUserRole()      { return fromUserRole; }
    public String getFromUserName()      { return fromUserName; }
    public Integer getToUserId()         { return toUserId; }
    public String getToUserRole()        { return toUserRole; }
}
