package com.bryan.service.impl;

import com.bryan.dto.request.UserAddressRequest;
import com.bryan.dto.response.UserAddressResponse;
import com.bryan.entity.ResidentialBuilding;
import com.bryan.entity.User;
import com.bryan.entity.UserAddress;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.UserAddressMapper;
import com.bryan.repository.ResidentialBuildingRepository;
import com.bryan.repository.UserAddressRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    private final ResidentialBuildingRepository buildingRepository;
    private final UserAddressMapper userAddressMapper;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getId()));
        }
        throw new BadRequestException("User must be authenticated");
    }

    private UserAddress getUserAddressAndVerifyOwner(Long id, User currentUser) {
        UserAddress address = userAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User address not found: " + id));
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You do not have permission to access this address");
        }
        return address;
    }

    private void handleDefaultAddressLogic(User user, boolean isNewDefault) {
        if (isNewDefault) {
            List<UserAddress> existingAddresses = userAddressRepository.findByUserId(user.getId());
            for (UserAddress addr : existingAddresses) {
                if (addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    userAddressRepository.save(addr);
                }
            }
        }
    }

    private ResidentialBuilding validateAndResolveBuilding(UserAddressRequest request) {
        if (request.buildingId() == null) {
            if (request.fullAddress() == null || request.fullAddress().isBlank()) {
                throw new BadRequestException("Full address is required when no building is selected");
            }
            return null;
        }

        ResidentialBuilding building = buildingRepository.findById(request.buildingId())
            .orElseThrow(() -> new BadRequestException("Building not found: " + request.buildingId()));
        if (!building.getIsActive()) {
            throw new BadRequestException("Building is not active: " + building.getCode());
        }
        if (request.floor() == null || request.floor().isBlank()) {
            throw new BadRequestException("Floor is required when building is selected");
        }
        if (request.apartmentNumber() == null || request.apartmentNumber().isBlank()) {
            throw new BadRequestException("Apartment number is required when building is selected");
        }
        if (request.recipientName() == null || request.recipientName().isBlank()) {
            throw new BadRequestException("Recipient name is required");
        }
        if (request.recipientPhone() == null || request.recipientPhone().isBlank()) {
            throw new BadRequestException("Recipient phone is required");
        }
        return building;
    }

    private String buildInternalFullAddress(UserAddressRequest request, ResidentialBuilding building) {
        return "Căn hộ %s, tầng %s, %s".formatted(
            request.apartmentNumber().trim(),
            request.floor().trim(),
            building.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressResponse> getAllMyAddresses() {
        User currentUser = getCurrentUser();
        List<UserAddress> addresses = userAddressRepository.findByUserId(currentUser.getId());
        return userAddressMapper.toResponseList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressResponse getMyAddressById(Long id) {
        User currentUser = getCurrentUser();
        UserAddress address = getUserAddressAndVerifyOwner(id, currentUser);
        return userAddressMapper.toResponse(address);
    }

    @Override
    public UserAddressResponse createMyAddress(UserAddressRequest request) {
        User currentUser = getCurrentUser();
        ResidentialBuilding building = validateAndResolveBuilding(request);
        handleDefaultAddressLogic(currentUser, request.isDefault());

        UserAddress address = userAddressMapper.toEntity(request);
        address.setUser(currentUser);

        if (building != null) {
            address.setBuilding(building);
            address.setFullAddress(buildInternalFullAddress(request, building));
        }

        UserAddress savedAddress = userAddressRepository.save(address);
        return userAddressMapper.toResponse(savedAddress);
    }

    @Override
    public UserAddressResponse updateMyAddress(Long id, UserAddressRequest request) {
        User currentUser = getCurrentUser();
        UserAddress existingAddress = getUserAddressAndVerifyOwner(id, currentUser);
        ResidentialBuilding building = validateAndResolveBuilding(request);

        if (request.isDefault() && !existingAddress.getIsDefault()) {
            handleDefaultAddressLogic(currentUser, true);
        }

        userAddressMapper.updateEntityFromRequest(request, existingAddress);

        if (building != null) {
            existingAddress.setBuilding(building);
            existingAddress.setFullAddress(buildInternalFullAddress(request, building));
        } else {
            existingAddress.setBuilding(null);
        }

        UserAddress updatedAddress = userAddressRepository.save(existingAddress);
        return userAddressMapper.toResponse(updatedAddress);
    }

    @Override
    public void deleteMyAddress(Long id) {
        User currentUser = getCurrentUser();
        UserAddress existingAddress = getUserAddressAndVerifyOwner(id, currentUser);
        userAddressRepository.delete(existingAddress);
    }
}
