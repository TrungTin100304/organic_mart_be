package com.bryan.service.impl;

import com.bryan.dto.request.CreateReviewRequest;
import com.bryan.dto.response.ReviewResponse;
import com.bryan.entity.Order;
import com.bryan.entity.Product;
import com.bryan.entity.Review;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.repository.ReviewRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndReviewStatus(productId, Review.ReviewStatus.APPROVED, pageable)
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getProduct().getId(),
                        review.getUser().getId(),
                        review.getUser().getFullName(),
                        review.getRating(),
                        review.getComment(),
                        review.getCreatedAt()));
    }

    @Override
    public ReviewResponse createReview(CreateReviewRequest request) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean hasPurchased = order.getUser().getId().equals(user.getId());
        if (!hasPurchased) {
            throw new BadRequestException("You can only review products from your own orders");
        }

        boolean alreadyReviewed = reviewRepository.findAll().stream()
                .anyMatch(r -> r.getUser().getId().equals(user.getId()) && r.getProduct().getId().equals(product.getId()));
        if (alreadyReviewed) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setReviewStatus(Review.ReviewStatus.PENDING);

        Review saved = reviewRepository.save(review);
        log.info("Review created for product {} by user {}", product.getId(), user.getId());

        return new ReviewResponse(
                saved.getId(),
                saved.getProduct().getId(),
                saved.getUser().getId(),
                saved.getUser().getFullName(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt());
    }

    @Override
    public void reportReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setReportReason(reason);
        review.setReviewStatus(Review.ReviewStatus.REJECTED);
        reviewRepository.save(review);
        log.info("Review {} reported: {}", reviewId, reason);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            return ud.getId();
        }
        throw new BadRequestException("User not authenticated");
    }
}
