package com.bryan.service;

import com.bryan.dto.request.FarmRequest;
import com.bryan.dto.response.FarmResponse;

import java.util.List;

public interface FarmService {
    List<FarmResponse> getAllFarms();

    FarmResponse getFarmById(Long id);

    FarmResponse createFarm(FarmRequest request);

    FarmResponse updateFarm(Long id, FarmRequest request);

    void deleteFarm(Long id);
}
