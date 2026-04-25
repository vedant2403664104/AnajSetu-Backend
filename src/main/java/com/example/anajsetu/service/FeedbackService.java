package com.example.anajsetu.service;

import com.example.anajsetu.dto.FeedbackResponseDto;
import com.example.anajsetu.model.Feedback;
import com.example.anajsetu.model.User;
import com.example.anajsetu.repository.FeedbackRepository;
import com.example.anajsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FeedbackService handles all business logic for the Feedback module.
 *
 * Design intent:
 *  - Feedback can be given by any user to any other user after an interaction.
 *  - Roles: DONOR, NGO, DELIVERY — all can give and receive feedback.
 *  - listingId is optional; feedback can exist without a listing reference
 *    (but must have fromUserId, toUserId, rating, feedbackType).
 *  - Duplicate feedback is blocked: same (listingId + fromUserId + toUserId + feedbackType)
 *    cannot be submitted twice.
 *  - All retrieval methods return FeedbackResponseDto which includes resolved
 *    sender display name (fromUserName) for frontend rendering.
 */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    // ── SUBMIT FEEDBACK ──────────────────────────────────────────────────────

    /**
     * Validates and saves a new feedback record.
     *
     * @param feedback the incoming feedback payload
     * @return the persisted Feedback entity
     * @throws IllegalArgumentException for missing/invalid fields
     * @throws IllegalStateException    for duplicate submissions or non-existent users
     */
    public Feedback saveFeedback(Feedback feedback) {

        // ── Required field validation ────────────────────────────────────────
        if (feedback.getFromUserId() == null) {
            throw new IllegalArgumentException("fromUserId is required.");
        }
        if (feedback.getToUserId() == null) {
            throw new IllegalArgumentException("toUserId is required.");
        }
        if (feedback.getFromUserId().equals(feedback.getToUserId())) {
            throw new IllegalArgumentException("A user cannot submit feedback for themselves.");
        }
        if (feedback.getFromUserRole() == null || feedback.getFromUserRole().isBlank()) {
            throw new IllegalArgumentException("fromUserRole is required.");
        }
        if (feedback.getToUserRole() == null || feedback.getToUserRole().isBlank()) {
            throw new IllegalArgumentException("toUserRole is required.");
        }
        if (feedback.getFeedbackType() == null || feedback.getFeedbackType().isBlank()) {
            throw new IllegalArgumentException("feedbackType is required (e.g. DONATION, CLAIM, DELIVERY, GENERAL).");
        }
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5.");
        }

        // ── User existence check ─────────────────────────────────────────────
        Optional<User> fromUser = userRepository.findById(feedback.getFromUserId());
        if (fromUser.isEmpty()) {
            throw new IllegalStateException(
                "Sender user not found (fromUserId=" + feedback.getFromUserId() + ")."
            );
        }
        Optional<User> toUser = userRepository.findById(feedback.getToUserId());
        if (toUser.isEmpty()) {
            throw new IllegalStateException(
                "Receiver user not found (toUserId=" + feedback.getToUserId() + ")."
            );
        }

        // ── Duplicate check ──────────────────────────────────────────────────
        boolean alreadyExists;
        if (feedback.getListingId() != null) {
            alreadyExists = feedbackRepository
                .existsByListingIdAndFromUserIdAndToUserIdAndFeedbackType(
                    feedback.getListingId(),
                    feedback.getFromUserId(),
                    feedback.getToUserId(),
                    feedback.getFeedbackType()
                );
        } else {
            alreadyExists = feedbackRepository
                .existsByListingIdIsNullAndFromUserIdAndToUserIdAndFeedbackType(
                    feedback.getFromUserId(),
                    feedback.getToUserId(),
                    feedback.getFeedbackType()
                );
        }
        if (alreadyExists) {
            throw new IllegalStateException(
                "Feedback of type '" + feedback.getFeedbackType() +
                "' has already been submitted by this user for the same receiver/listing."
            );
        }

        System.out.println("[FeedbackService] Saving feedback: fromUserId=" + feedback.getFromUserId()
            + " (" + feedback.getFromUserRole() + ")"
            + " -> toUserId=" + feedback.getToUserId()
            + " (" + feedback.getToUserRole() + ")"
            + ", type=" + feedback.getFeedbackType()
            + ", rating=" + feedback.getRating()
            + ", listingId=" + feedback.getListingId());

        return feedbackRepository.save(feedback);
    }

    // ── GET RECEIVED FEEDBACK ────────────────────────────────────────────────

    /**
     * Returns all feedback received by a specific user, sorted newest first.
     * Supports any role — DONOR, NGO, DELIVERY.
     */
    public List<FeedbackResponseDto> getReceivedFeedback(Integer toUserId) {
        validateUserExists(toUserId, "toUserId");
        return feedbackRepository
                .findByToUserIdOrderByCreatedAtDesc(toUserId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Returns received feedback filtered by feedback type.
     * Example: get only "DELIVERY" type feedback for a delivery partner.
     */
    public List<FeedbackResponseDto> getReceivedFeedbackByType(Integer toUserId, String feedbackType) {
        validateUserExists(toUserId, "toUserId");
        return feedbackRepository
                .findByToUserIdAndFeedbackTypeOrderByCreatedAtDesc(toUserId, feedbackType)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── GET GIVEN FEEDBACK ───────────────────────────────────────────────────

    /** Returns all feedback submitted by a specific user, sorted newest first. */
    public List<FeedbackResponseDto> getGivenFeedback(Integer fromUserId) {
        validateUserExists(fromUserId, "fromUserId");
        return feedbackRepository
                .findByFromUserIdOrderByCreatedAtDesc(fromUserId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── GET BY LISTING ───────────────────────────────────────────────────────

    /** Returns all feedback associated with a specific food listing. */
    public List<FeedbackResponseDto> getFeedbackByListing(Integer listingId) {
        if (listingId == null) {
            throw new IllegalArgumentException("listingId must be provided.");
        }
        return feedbackRepository
                .findByListingIdOrderByCreatedAtDesc(listingId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── AVERAGE RATING ───────────────────────────────────────────────────────

    /**
     * Returns the average rating received by a user.
     * Returns 0.0 if no feedback exists yet.
     */
    public double getAverageRating(Integer toUserId) {
        validateUserExists(toUserId, "toUserId");
        Double avg = feedbackRepository.findAverageRatingByToUserId(toUserId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    /**
     * Returns the average rating received by a user for a specific feedback type.
     */
    public double getAverageRatingByType(Integer toUserId, String feedbackType) {
        validateUserExists(toUserId, "toUserId");
        Double avg = feedbackRepository.findAverageRatingByToUserIdAndFeedbackType(toUserId, feedbackType);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ── GET FEEDBACK COUNT ───────────────────────────────────────────────────

    /** Returns the total count of feedback received by a user. */
    public long getFeedbackCount(Integer toUserId) {
        validateUserExists(toUserId, "toUserId");
        return feedbackRepository.countByToUserId(toUserId);
    }

    // ── INTERNAL HELPERS ─────────────────────────────────────────────────────

    /**
     * Maps a Feedback entity to FeedbackResponseDto.
     *
     * Resolves the sender display name by looking up fromUserId in the users table.
     * Uses User.name as the display field (the only name field present in User entity).
     * Falls back to null safely if the user record is not found.
     */
    private FeedbackResponseDto toDto(Feedback f) {
        String fromUserName = userRepository.findById(f.getFromUserId())
                .map(User::getName)
                .orElse(null);

        return new FeedbackResponseDto(
                f.getId(),
                f.getListingId(),
                f.getRating(),
                f.getComment(),
                f.getCreatedAt(),
                f.getFeedbackType(),
                f.getFromUserId(),
                f.getFromUserRole(),
                fromUserName,
                f.getToUserId(),
                f.getToUserRole()
        );
    }

    private void validateUserExists(Integer userId, String fieldName) {
        if (userId == null) {
            throw new IllegalArgumentException(fieldName + " must be provided.");
        }
        if (userRepository.findById(userId).isEmpty()) {
            throw new IllegalStateException("User not found (" + fieldName + "=" + userId + ").");
        }
    }
}