package com.bryan.service.impl;

import com.bryan.dto.request.UserStatusRequest;
import com.bryan.dto.request.UserUpdateRequest;
import com.bryan.dto.response.UserResponse;
import com.bryan.entity.Role;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.mapper.UserMapper;
import com.bryan.repository.UserRepository;
import com.bryan.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Customer");
        user.setPhoneNumber("0909000000");
        user.setEmail("customer@test.dev");
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
    }

    @Test
    void shouldUpdateUserStatusFromMultipartStatusField() {
        UserUpdateRequest request = new UserUpdateRequest("Customer", "0909000000", null, null, null, "locked");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doAnswer(invocation -> null).when(userMapper).updateUser(request, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        UserResponse response = userService.updateUser(1L, request);

        assertFalse(user.isActive());
        assertFalse(response.isActive());
    }

    @Test
    void shouldUpdateUserStatusWithDedicatedStatusRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        UserResponse inactive = userService.updateUserStatus(1L, new UserStatusRequest(null, null, "inactive"));
        assertFalse(user.isActive());
        assertFalse(inactive.isActive());

        UserResponse active = userService.updateUserStatus(1L, new UserStatusRequest(true, null, null));
        assertTrue(user.isActive());
        assertTrue(active.isActive());
    }

    @Test
    void shouldRejectUnknownUserStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () ->
                userService.updateUserStatus(1L, new UserStatusRequest(null, null, "paused")));
    }

    private UserResponse toResponse(User updated) {
        return new UserResponse(
                updated.getId(),
                updated.getFullName(),
                updated.getPhoneNumber(),
                updated.getEmail(),
                updated.getAvatarUrl(),
                updated.isActive(),
                updated.getRole(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
