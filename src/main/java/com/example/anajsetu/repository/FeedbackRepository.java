package com.example.anajsetu.repository;

import com.example.anajsetu.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ── Received Feedback (by toUserId) ─────────────────────────────────────

    /** All feedback received by a user, newest first. */
    List<Feedback> findByToUserIdOrderByCreatedAtDesc(Integer toUserId);

    /** Received feedback filtered by feedback type (e.g. "DONATION", "DELIVERY"). */
    List<Feedback> findByToUserIdAndFeedbackTypeOrderByCreatedAtDesc(
            Integer toUserId, String feedbackType);

    // ── Given Feedback (by fromUserId) ──────────────────────────────────────

    /** All feedback given by a user, newest first. */
    List<Feedback> findByFromUserIdOrderByCreatedAtDesc(Integer fromUserId);

    // ── Listing-level Feedback ───────────────────────────────────────────────

    /** All feedback linked to a specific food listing, newest first. */
    List<Feedback> findByListingIdOrderByCreatedAtDesc(Integer listingId);

    // ── Duplicate prevention ─────────────────────────────────────────────────

    /**
     * Check if a specific user has already submitted the same feedback type
     * for the same listing and receiver. Prevents duplicate submissions.
     */
    boolean existsByListingIdAndFromUserIdAndToUserIdAndFeedbackType(
            Integer listingId,
            Integer fromUserId,
            Integer toUserId,
            String feedbackType);

    /**
     * For feedback NOT tied to any listing (listingId = null), check duplicates
     * by fromUserId, toUserId, and feedbackType only.
     */
    boolean existsByListingIdIsNullAndFromUserIdAndToUserIdAndFeedbackType(
            Integer fromUserId,
            Integer toUserId,
            String feedbackType);

    // ── Average rating ───────────────────────────────────────────────────────

    /** Returns the average rating received by a user across all feedback. */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.toUserId = :toUserId")
    Double findAverageRatingByToUserId(@Param("toUserId") Integer toUserId);

    /** Returns the average rating received by a user for a specific feedback type. */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.toUserId = :toUserId AND f.feedbackType = :feedbackType")
    Double findAverageRatingByToUserIdAndFeedbackType(
            @Param("toUserId") Integer toUserId,
            @Param("feedbackType") String feedbackType);

    // ── Count ────────────────────────────────────────────────────────────────

    /** Total count of feedback received by a user. */
    long countByToUserId(Integer toUserId);
}