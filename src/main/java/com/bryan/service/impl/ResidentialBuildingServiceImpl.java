package com.bryan.service.impl;

import com.bryan.dto.request.ResidentialBuildingRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.ResidentialBuildingResponse;
import com.bryan.entity.ResidentialBuilding;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.ResidentialBuildingRepository;
import com.bryan.service.ResidentialBuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResidentialBuildingServiceImpl implements ResidentialBuildingService {

    private final ResidentialBuildingRepository buildingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ResidentialBuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
            .map(ResidentialBuildingResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentialBuildingResponse> getActiveBuildings() {
        return buildingRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
            .map(ResidentialBuildingResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentialBuildingResponse getBuildingById(Long id) {
        return buildingRepository.findById(id)
            .map(ResidentialBuildingResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + id));
    }

    @Override
    public ResidentialBuildingResponse createBuilding(ResidentialBuildingRequest request) {
        if (buildingRepository.existsByCode(request.code())) {
            throw new BadRequestException("Building code already exists: " + request.code());
        }
        ResidentialBuilding building = new ResidentialBuilding(
            request.code(),
            request.name(),
            request.description(),
            request.displayOrder()
        );
        ResidentialBuilding saved = buildingRepository.save(building);
        log.info("Created residential building: code={}, name={}", saved.getCode(), saved.getName());
        return ResidentialBuildingResponse.from(saved);
    }

    @Override
    public ResidentialBuildingResponse updateBuilding(Long id, ResidentialBuildingRequest request) {
        ResidentialBuilding building = buildingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + id));

        if (buildingRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new BadRequestException("Building code already exists: " + request.code());
        }

        building.setCode(request.code());
        building.setName(request.name());
        building.setDescription(request.description());
        if (request.displayOrder() != null) {
            building.setDisplayOrder(request.displayOrder());
        }

        ResidentialBuilding saved = buildingRepository.save(building);
        log.info("Updated residential building: id={}, code={}", saved.getId(), saved.getCode());
        return ResidentialBuildingResponse.from(saved);
    }

    @Override
    public ResidentialBuildingResponse updateStatus(Long id, StatusRequest request) {
        ResidentialBuilding building = buildingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + id));
        building.setIsActive(request.isActive());
        ResidentialBuilding saved = buildingRepository.save(building);
        log.info("Updated building status: id={}, isActive={}", saved.getId(), saved.getIsActive());
        return ResidentialBuildingResponse.from(saved);
    }
}
