package com.bryan.service;

import com.bryan.dto.request.UserAddressRequest;
import com.bryan.dto.response.UserAddressResponse;

import java.util.List;

public interface UserAddressService {
    List<UserAddressResponse> getAllMyAddresses();
    UserAddressResponse getMyAddressById(Long id);
    UserAddressResponse createMyAddress(UserAddressRequest request);
    UserAddressResponse updateMyAddress(Long id, UserAddressRequest request);
    void deleteMyAddress(Long id);

    // Admin methods if needed later could go here like getAllAddressesByUserId(Long userId)
}
