package com.bryan.service.impl;

import com.bryan.dto.request.AddCartItemRequest;
import com.bryan.dto.request.UpdateCartItemQuantityRequest;
import com.bryan.dto.response.CartResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.CartItem;
import com.bryan.entity.Product;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.CartMapper;
import com.bryan.repository.CartRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCurrentCart() {
        User user = getAuthenticatedUser();
        return cartMapper.toResponse(getOrCreateCart(user));
    }

    @Override
    public CartResponse addItem(AddCartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        Product product = getProductById(request.productId());

        CartItem item = findItem(cart, product.getId()).orElseGet(() -> {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(BigDecimal.ZERO);
            cart.addItem(newItem);
            return newItem;
        });

        item.setQuantity(item.getQuantity().add(request.quantity()));
        touchAndSave(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse updateItemQuantity(Long productId, UpdateCartItemQuantityRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id: " + productId + " not found in cart"));

        BigDecimal currentQuantityInCart = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;

        BigDecimal targetQuantity = currentQuantityInCart.subtract(request.quantity());

        if (targetQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Số lượng trong giỏ không đủ để giảm thêm");
        }

        if (targetQuantity.compareTo(BigDecimal.ZERO) == 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(targetQuantity);
        }

        touchAndSave(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long productId) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = findItem(cart, productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product with id: " + productId + " not found in cart"));

        cart.removeItem(item);
        touchAndSave(cart);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        touchAndSave(cart);
        return cartMapper.toResponse(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                cart.touch();
                return cartRepository.save(cart);
            });
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private java.util.Optional<CartItem> findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
            .filter(item -> item.getProduct() != null && item.getProduct().getId() != null && item.getProduct().getId().equals(productId))
            .findFirst();
    }

    private void touchAndSave(Cart cart) {
        cart.touch();
        cartRepository.save(cart);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("Authentication required");
        }

        return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));
    }
}
