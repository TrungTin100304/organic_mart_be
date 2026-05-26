package com.bryan.service;

import com.bryan.dto.request.UserUpdateRequest;
import com.bryan.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UserUpdateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}

