package com.bryan.service.impl;

import com.bryan.dto.request.FarmRequest;
import com.bryan.dto.response.FarmResponse;
import com.bryan.entity.Farm;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.FarmMapper;
import com.bryan.repository.FarmRepository;
import com.bryan.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;
    private final FarmMapper farmMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FarmResponse> getAllFarms() {
        return farmRepository.findAll().stream().map(farmMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FarmResponse getFarmById(Long id) {
        return farmMapper.toResponse(getFarmEntityById(id));
    }

    @Override
    public FarmResponse createFarm(FarmRequest request) {
        Farm farm = farmMapper.toEntity(request);
        Farm savedFarm = farmRepository.save(farm);
        return farmMapper.toResponse(savedFarm);
    }

    @Override
    public FarmResponse updateFarm(Long id, FarmRequest request) {
        Farm farm = getFarmEntityById(id);
        farmMapper.updateEntity(request, farm);
        return farmMapper.toResponse(farm);
    }

    @Override
    public void deleteFarm(Long id) {
        if (!farmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Farm not found with id: " + id);
        }
        farmRepository.deleteById(id);
    }

    private Farm getFarmEntityById(Long id) {
        return farmRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
    }
}

