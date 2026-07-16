package com.bryan.controller.admin;

import com.bryan.dto.response.AdminReviewResponse;
import com.bryan.dto.response.ApiResponse;
import com.bryan.entity.Review;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.ReviewRepository;
import com.bryan.repository.specification.AdminSearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminReviewResponse>>> getReviews(
            @RequestParam(required = false) Review.ReviewStatus status,
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(reviewRepository
                .findAll(AdminSearchSpecifications.reviews(status, productName), pageable)
                .map(AdminReviewResponse::from));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {
        updateStatus(id, Review.ReviewStatus.APPROVED);
        return ApiResponse.success(null, "Review approved");
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long id) {
        updateStatus(id, Review.ReviewStatus.REJECTED);
        return ApiResponse.success(null, "Review rejected");
    }

    private void updateStatus(Long id, Review.ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        review.setReviewStatus(status);
        reviewRepository.save(review);
    }
}
