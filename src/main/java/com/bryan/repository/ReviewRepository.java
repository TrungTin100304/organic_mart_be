package com.bryan.repository;

import com.bryan.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    Page<Review> findByProductIdAndReviewStatus(Long productId, Review.ReviewStatus status, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);
}
