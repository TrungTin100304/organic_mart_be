package com.bryan.dto.response;

import com.bryan.entity.Review;

import java.time.LocalDateTime;

public record AdminReviewResponse(
        Long id,
        Long productId,
        String productName,
        Long userId,
        String userFullName,
        Integer rating,
        String comment,
        Review.ReviewStatus reviewStatus,
        String reportReason,
        LocalDateTime createdAt
) {
    public static AdminReviewResponse from(Review review) {
        return new AdminReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getReviewStatus(),
                review.getReportReason(),
                review.getCreatedAt());
    }
}
