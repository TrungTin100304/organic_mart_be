package com.bryan.service.impl;

import com.bryan.dto.request.CreateReviewRequest;
import com.bryan.entity.Order;
import com.bryan.entity.Product;
import com.bryan.entity.Review;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.OrderDetailRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.repository.ReviewRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock ReviewRepository reviewRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ReviewServiceImpl service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsPendingReviewOnlyForDeliveredPurchase() {
        authenticate(1L);
        User user = new User();
        user.setId(1L);
        user.setFullName("Buyer");
        Product product = new Product();
        product.setId(2L);
        Order order = new Order();
        order.setId(3L);
        CreateReviewRequest request = new CreateReviewRequest(2L, 3L, 5, "Fresh");

        when(orderDetailRepository.existsDeliveredPurchase(1L, 2L, 3L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(4L);
            return review;
        });

        var response = service.createReview(request);

        assertEquals(4L, response.id());
        assertEquals(5, response.rating());
    }

    @Test
    void rejectsReviewWhenProductWasNotInDeliveredOrder() {
        authenticate(1L);
        when(orderDetailRepository.existsDeliveredPurchase(1L, 2L, 3L)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> service.createReview(new CreateReviewRequest(2L, 3L, 5, "Fresh")));
    }

    private void authenticate(Long id) {
        CustomUserDetails principal = new CustomUserDetails(
                id, "buyer@test.dev", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
