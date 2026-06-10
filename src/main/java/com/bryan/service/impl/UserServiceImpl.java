package com.bryan.service.impl;

import com.bryan.dto.request.UserUpdateRequest;
import com.bryan.dto.request.UserStatusRequest;
import com.bryan.dto.response.UserResponse;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.UserMapper;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.UserService;
import com.bryan.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.toResponse(getAuthenticatedUser());
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        User user = getAuthenticatedUser();
        userMapper.updateUser(request, user);

        if (request.avatar() != null && !request.avatar().isEmpty()) {
            String avatarUrl = fileUploadService.uploadFile(request.avatar(), "organic-mart/avatars");
            user.setAvatarUrl(avatarUrl);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserById(id);
        userMapper.updateUser(request, user);
        resolveActiveStatus(request.isActive(), request.active(), request.status())
                .ifPresent(user::setActive);

        if (request.avatar() != null && !request.avatar().isEmpty()) {
            String avatarUrl = fileUploadService.uploadFile(request.avatar(), "organic-mart/avatars");
            user.setAvatarUrl(avatarUrl);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUserStatus(Long id, UserStatusRequest request) {
        User user = findUserById(id);
        Boolean active = resolveActiveStatus(request.isActive(), request.active(), request.status())
                .orElseThrow(() -> new BadRequestException("User status is required"));
        user.setActive(active);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private java.util.Optional<Boolean> resolveActiveStatus(Boolean isActive, Boolean active, String status) {
        if (isActive != null) {
            return java.util.Optional.of(isActive);
        }
        if (active != null) {
            return java.util.Optional.of(active);
        }
        if (status == null || status.isBlank()) {
            return java.util.Optional.empty();
        }

        return switch (status.trim().toLowerCase()) {
            case "active", "enabled", "enable", "true" -> java.util.Optional.of(true);
            case "inactive", "locked", "disabled", "disable", "false" -> java.util.Optional.of(false);
            default -> throw new BadRequestException("Unsupported user status: " + status);
        };
    }

    private User getAuthenticatedUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findUserById(userDetails.getId());
    }
}
