package com.bryan.controller;

import com.bryan.dto.request.CreateReviewRequest;
import com.bryan.dto.request.ReviewReportRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ReviewResponse;
import com.bryan.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getApprovedReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(reviewService.getApprovedReviews(
                productId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(201, reviewService.createReview(request));
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> report(
            @PathVariable Long id,
            @Valid @RequestBody ReviewReportRequest request) {
        reviewService.reportReview(id, request.reason());
        return ApiResponse.success(null, "Review reported");
    }
}
