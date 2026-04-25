package com.example.anajsetu.controller;

import com.example.anajsetu.dto.FeedbackResponseDto;
import com.example.anajsetu.model.Feedback;
import com.example.anajsetu.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the Feedback module.
 *
 * Base URL: /api/feedback
 *
 * Endpoints:
 * POST /api/feedback                              — Submit new feedback
 * GET  /api/feedback/received/{userId}            — Get all feedback received by a user
 * GET  /api/feedback/received/{userId}/type/{type}— Get received feedback filtered by type
 * GET  /api/feedback/given/{userId}               — Get all feedback given by a user
 * GET  /api/feedback/listing/{listingId}          — Get all feedback for a listing
 * GET  /api/feedback/rating/{userId}              — Get average rating for a user
 * GET  /api/feedback/rating/{userId}/type/{type}  — Get average rating by feedback type
 * GET  /api/feedback/count/{userId}               — Get total feedback count for a user
 *
 * All list-returning GET endpoints now return FeedbackResponseDto which includes
 * the resolved sender name (fromUserName) in addition to raw entity fields.
 */
@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:3000")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ── POST /api/feedback ───────────────────────────────────────────────────

    /**
     * Submit new feedback.
     *
     * Request body example:
     * {
     *   "listingId": 12,         // optional — omit or null if not listing-related
     *   "fromUserId": 3,
     *   "fromUserRole": "NGO",
     *   "toUserId": 7,
     *   "toUserRole": "DONOR",
     *   "rating": 5,
     *   "comment": "Great donor, food was fresh and on time!",
     *   "feedbackType": "DONATION"
     * }
     *
     * Returns 201 Created on success, 400 on validation error, 409 on duplicate,
     * 500 otherwise.
     */
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Feedback feedback) {
        System.out.println("[FeedbackController] POST /api/feedback called");
        System.out.println("  listingId    = " + feedback.getListingId());
        System.out.println("  fromUserId   = " + feedback.getFromUserId() + " (" + feedback.getFromUserRole() + ")");
        System.out.println("  toUserId     = " + feedback.getToUserId() + " (" + feedback.getToUserRole() + ")");
        System.out.println("  rating       = " + feedback.getRating());
        System.out.println("  feedbackType = " + feedback.getFeedbackType());
        System.out.println("  comment      = " + feedback.getComment());

        try {
            Feedback saved = feedbackService.saveFeedback(feedback);
            System.out.println("[FeedbackController] Feedback saved, id=" + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IllegalArgumentException e) {
            System.out.println("[FeedbackController] Validation error: " + e.getMessage());
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalStateException e) {
            System.out.println("[FeedbackController] Conflict: " + e.getMessage());
            return buildError(HttpStatus.CONFLICT, e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected error while saving feedback: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/received/{userId} ──────────────────────────────────

    /**
     * Get all feedback received by a user (any role — DONOR, NGO, DELIVERY).
     * Response includes fromUserName so the frontend can display sender identity.
     */
    @GetMapping("/received/{userId}")
    public ResponseEntity<?> getReceivedFeedback(@PathVariable Integer userId) {
        try {
            List<FeedbackResponseDto> list = feedbackService.getReceivedFeedback(userId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching received feedback: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/received/{userId}/type/{feedbackType} ─────────────

    /**
     * Get received feedback filtered by feedback type.
     * Example: GET /api/feedback/received/5/type/DELIVERY
     */
    @GetMapping("/received/{userId}/type/{feedbackType}")
    public ResponseEntity<?> getReceivedFeedbackByType(
            @PathVariable Integer userId,
            @PathVariable String feedbackType) {
        try {
            List<FeedbackResponseDto> list = feedbackService.getReceivedFeedbackByType(userId, feedbackType);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching received feedback by type: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/given/{userId} ─────────────────────────────────────

    /** Get all feedback submitted by a user. */
    @GetMapping("/given/{userId}")
    public ResponseEntity<?> getGivenFeedback(@PathVariable Integer userId) {
        try {
            List<FeedbackResponseDto> list = feedbackService.getGivenFeedback(userId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching given feedback: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/listing/{listingId} ────────────────────────────────

    /** Get all feedback associated with a specific food listing. */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getFeedbackByListing(@PathVariable Integer listingId) {
        try {
            List<FeedbackResponseDto> list = feedbackService.getFeedbackByListing(listingId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching feedback for listing: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/rating/{userId} ────────────────────────────────────

    /**
     * Get the overall average rating received by a user.
     * Returns: { "userId": 5, "averageRating": 4.3, "totalFeedback": 12 }
     */
    @GetMapping("/rating/{userId}")
    public ResponseEntity<?> getAverageRating(@PathVariable Integer userId) {
        try {
            double avg   = feedbackService.getAverageRating(userId);
            long   count = feedbackService.getFeedbackCount(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("averageRating", avg);
            result.put("totalFeedback", count);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error calculating average rating: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/rating/{userId}/type/{feedbackType} ────────────────

    /**
     * Get the average rating received by a user for a specific feedback type.
     * Example: GET /api/feedback/rating/5/type/DELIVERY
     * Returns: { "userId": 5, "feedbackType": "DELIVERY", "averageRating": 4.7 }
     */
    @GetMapping("/rating/{userId}/type/{feedbackType}")
    public ResponseEntity<?> getAverageRatingByType(
            @PathVariable Integer userId,
            @PathVariable String feedbackType) {
        try {
            double avg = feedbackService.getAverageRatingByType(userId, feedbackType);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("feedbackType", feedbackType);
            result.put("averageRating", avg);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error calculating average rating by type: " + e.getMessage());
        }
    }

    // ── GET /api/feedback/count/{userId} ─────────────────────────────────────

    /**
     * Get the total number of feedback records received by a user.
     * Returns: { "userId": 5, "totalFeedback": 12 }
     */
    @GetMapping("/count/{userId}")
    public ResponseEntity<?> getFeedbackCount(@PathVariable Integer userId) {
        try {
            long count = feedbackService.getFeedbackCount(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("totalFeedback", count);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching feedback count: " + e.getMessage());
        }
    }

    // ── Helper: build consistent error response ───────────────────────────────

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}