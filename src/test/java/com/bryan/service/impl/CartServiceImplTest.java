package com.bryan.service.impl;

import com.bryan.dto.request.AddCartItemRequest;
import com.bryan.dto.request.UpdateCartItemQuantityRequest;
import com.bryan.dto.response.CartItemResponse;
import com.bryan.dto.response.CartResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.CartItem;
import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;
import com.bryan.entity.Role;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.CartMapper;
import com.bryan.repository.CartRepository;
import com.bryan.repository.ProductRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@example.com");
        user.setRole(Role.ROLE_USER);

        product = new Product();
        product.setId(100L);
        product.setName("Organic Apple");
        product.setSlug("organic-apple");
        product.setPrice(new BigDecimal("10.00"));
        product.setUnit("kg");
        product.setImageUrl("https://cdn.example.com/apple.jpg");
        product.setCategory(new ProductCategory());

        cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        cart.setItems(new java.util.ArrayList<>());
        cart.setUpdatedAt(LocalDateTime.now());

        CartItem cartItem = new CartItem();
        cartItem.setId(200L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(new BigDecimal("1.50"));
        cartItem.setAddedAt(LocalDateTime.now());
        cart.getItems().add(cartItem);

        CustomUserDetails principal = new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnEmptyCart_whenCartDoesNotExist() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartMapper.toResponse(any(Cart.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CartResponse response = cartService.getCurrentCart();

        assertEquals(user.getId(), response.userId());
        assertEquals(BigDecimal.ZERO, response.totalQuantity());
        assertEquals(BigDecimal.ZERO, response.totalPrice());
        assertEquals(0, response.distinctItemCount());
        assertEquals(0, response.items().size());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldAddItemAndCreateCart_whenCartDoesNotExist() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartMapper.toResponse(any(Cart.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CartResponse response = cartService.addItem(new AddCartItemRequest(product.getId(), new BigDecimal("2.50")));

        assertEquals(1, response.distinctItemCount());
        assertEquals(new BigDecimal("2.50"), response.totalQuantity());
        assertEquals(new BigDecimal("25.0000"), response.totalPrice());
        assertEquals(1, response.items().size());
        assertEquals(new BigDecimal("2.50"), response.items().getFirst().quantity());
        verify(cartRepository, atLeast(2)).save(any(Cart.class));
    }

    @Test
    void shouldIncrementExistingQuantity_whenAddingSameProduct() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartMapper.toResponse(any(Cart.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CartResponse response = cartService.addItem(new AddCartItemRequest(product.getId(), new BigDecimal("0.50")));

        assertEquals(new BigDecimal("2.00"), response.totalQuantity());
        assertEquals(new BigDecimal("20.0000"), response.totalPrice());
        assertEquals(new BigDecimal("2.00"), response.items().getFirst().quantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowResourceNotFoundException_whenUpdatingMissingItem() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(new Cart()));

        assertThrows(ResourceNotFoundException.class,
            () -> cartService.updateItemQuantity(product.getId(), new UpdateCartItemQuantityRequest(new BigDecimal("3.00"))));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldDecreaseExistingQuantity_whenUpdatingItemQuantity() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartMapper.toResponse(any(Cart.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CartResponse response = cartService.updateItemQuantity(product.getId(), new UpdateCartItemQuantityRequest(new BigDecimal("0.50")));

        assertEquals(new BigDecimal("1.00"), response.totalQuantity());
        assertEquals(1, response.distinctItemCount());
        assertEquals(new BigDecimal("1.00"), response.items().getFirst().quantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldRemoveItem_whenQuantityBecomesZero() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartMapper.toResponse(any(Cart.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        CartResponse response = cartService.updateItemQuantity(product.getId(), new UpdateCartItemQuantityRequest(new BigDecimal("1.50")));

        assertEquals(BigDecimal.ZERO, response.totalQuantity());
        assertEquals(0, response.distinctItemCount());
        assertEquals(0, response.items().size());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowBadRequestException_whenRequestExceedsCurrentQuantity() {
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(BadRequestException.class,
            () -> cartService.updateItemQuantity(product.getId(), new UpdateCartItemQuantityRequest(new BigDecimal("2.00"))));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::toItemResponse)
            .toList();

        BigDecimal totalQuantity = items.stream()
            .map(CartItemResponse::quantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrice = items.stream()
            .map(CartItemResponse::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
            cart.getId(),
            cart.getUser() != null ? cart.getUser().getId() : null,
            totalQuantity,
            totalPrice,
            items.size(),
            cart.getUpdatedAt(),
            items
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal subtotal = item.getQuantity().multiply(item.getProduct().getPrice());
        return new CartItemResponse(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getSlug(),
            item.getProduct().getImageUrl(),
            item.getProduct().getPrice(),
            item.getProduct().getUnit(),
            item.getQuantity(),
            subtotal,
            item.getAddedAt()
        );
    }
}


