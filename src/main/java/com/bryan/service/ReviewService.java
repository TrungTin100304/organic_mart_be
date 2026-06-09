package com.bryan.service;

import com.bryan.dto.request.CreateReviewRequest;
import com.bryan.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;

public interface ReviewService {
    Page<ReviewResponse> getApprovedReviews(Long productId, org.springframework.data.domain.Pageable pageable);
    ReviewResponse createReview(CreateReviewRequest request);
    void reportReview(Long reviewId, String reason);
}
