package com.bryan.service;

import com.bryan.dto.request.ResidentialBuildingRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.ResidentialBuildingResponse;
import com.bryan.dto.response.UserAddressResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResidentialBuildingService {
    List<ResidentialBuildingResponse> getAllBuildings();
    List<ResidentialBuildingResponse> getActiveBuildings();
    ResidentialBuildingResponse getBuildingById(Long id);
    ResidentialBuildingResponse createBuilding(ResidentialBuildingRequest request);
    ResidentialBuildingResponse updateBuilding(Long id, ResidentialBuildingRequest request);
    ResidentialBuildingResponse updateStatus(Long id, StatusRequest request);
}
