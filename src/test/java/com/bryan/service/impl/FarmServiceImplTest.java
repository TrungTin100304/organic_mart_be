package com.bryan.service.impl;

import com.bryan.dto.request.FarmRequest;
import com.bryan.dto.response.FarmResponse;
import com.bryan.entity.Farm;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.FarmMapper;
import com.bryan.repository.FarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmMapper farmMapper;

    @InjectMocks
    private FarmServiceImpl farmService;

    private Farm farm;

    @BeforeEach
    void setUp() {
        farm = new Farm();
        farm.setId(1L);
        farm.setName("Green Farm");
        farm.setCertification("VietGAP");
        farm.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateFarm_success() {
        FarmRequest request = new FarmRequest("Green Farm", "VietGAP", "Da Lat", "0909", "farm@mail.com");
        FarmResponse response = new FarmResponse(1L, "Green Farm", "VietGAP", "Da Lat", "0909", "farm@mail.com", LocalDateTime.now());

        when(farmMapper.toEntity(request)).thenReturn(farm);
        when(farmRepository.save(farm)).thenReturn(farm);
        when(farmMapper.toResponse(farm)).thenReturn(response);

        FarmResponse result = farmService.createFarm(request);

        assertEquals(response, result);
        verify(farmRepository).save(farm);
    }

    @Test
    void shouldThrowResourceNotFoundException_whenFarmDoesNotExist() {
        when(farmRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> farmService.getFarmById(1L));
    }

    @Test
    void shouldDeleteFarm_success() {
        when(farmRepository.existsById(1L)).thenReturn(true);

        farmService.deleteFarm(1L);

        verify(farmRepository).deleteById(1L);
    }

    @Test
    void shouldUpdateFarm_success() {
        FarmRequest request = new FarmRequest("Updated Farm", "GlobalGAP", "Hue", "0911", "updated@mail.com");
        FarmResponse response = new FarmResponse(1L, "Updated Farm", "GlobalGAP", "Hue", "0911", "updated@mail.com", LocalDateTime.now());

        when(farmRepository.findById(1L)).thenReturn(Optional.of(farm));
        doAnswer(invocation -> {
            FarmRequest req = invocation.getArgument(0);
            Farm entity = invocation.getArgument(1);
            entity.setName(req.name());
            entity.setCertification(req.certification());
            entity.setLocation(req.location());
            entity.setContactPhone(req.contactPhone());
            entity.setContactEmail(req.contactEmail());
            return null;
        }).when(farmMapper).updateEntity(eq(request), any(Farm.class));
        when(farmMapper.toResponse(farm)).thenReturn(response);

        FarmResponse result = farmService.updateFarm(1L, request);

        assertEquals(response, result);
        assertEquals("Updated Farm", farm.getName());
    }
}

