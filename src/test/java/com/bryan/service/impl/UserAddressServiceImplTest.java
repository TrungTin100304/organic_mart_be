package com.bryan.service.impl;

import com.bryan.dto.request.UserAddressRequest;
import com.bryan.entity.AddressLabel;
import com.bryan.entity.ResidentialBuilding;
import com.bryan.entity.User;
import com.bryan.entity.UserAddress;
import com.bryan.exception.BadRequestException;
import com.bryan.mapper.UserAddressMapper;
import com.bryan.repository.ResidentialBuildingRepository;
import com.bryan.repository.UserAddressRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @Mock private UserAddressRepository userAddressRepository;
    @Mock private UserRepository userRepository;
    @Mock private ResidentialBuildingRepository buildingRepository;
    @Mock private UserAddressMapper userAddressMapper;
    @InjectMocks private UserAddressServiceImpl service;

    @BeforeEach
    void setUpAuthentication() {
        var principal = new CustomUserDetails(1L, "user@test.local", "password", List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsBlankFullAddressWhenNoBuildingIsSelected() {
        var request = new UserAddressRequest(
            AddressLabel.HOME,
            null,
            "Nguyen Van A",
            "0909000000",
            "",
            "",
            "",
            "",
            false,
            null,
            null,
            null,
            null
        );

        assertThrows(BadRequestException.class, () -> service.createMyAddress(request));
    }

    @Test
    void savesInternalAddressWithoutManualFullAddress() {
        var request = new UserAddressRequest(
            AddressLabel.HOME,
            null,
            "Nguyen Van A",
            "0909000000",
            "",
            "",
            "",
            "",
            false,
            12L,
            "5",
            "501",
            null
        );
        ResidentialBuilding building = new ResidentialBuilding();
        building.setId(12L);
        building.setCode("S1.01");
        building.setName("Tòa S1.01 - The Rainbow");
        building.setIsActive(true);
        UserAddress address = new UserAddress();

        when(buildingRepository.findById(12L)).thenReturn(Optional.of(building));
        when(userAddressMapper.toEntity(request)).thenReturn(address);
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createMyAddress(request);

        assertSame(building, address.getBuilding());
        assertEquals(
            "Căn hộ 501, tầng 5, Tòa S1.01 - The Rainbow",
            address.getFullAddress()
        );
    }
}
