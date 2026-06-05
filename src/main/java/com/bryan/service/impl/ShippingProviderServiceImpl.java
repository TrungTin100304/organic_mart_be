package com.bryan.service.impl;

import com.bryan.dto.request.ShippingProviderRequest;
import com.bryan.dto.response.ShippingProviderResponse;
import com.bryan.entity.ShippingProvider;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.ShippingProviderMapper;
import com.bryan.repository.ShippingProviderRepository;
import com.bryan.service.ShippingProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShippingProviderServiceImpl implements ShippingProviderService {

    private final ShippingProviderRepository providerRepository;
    private final ShippingProviderMapper providerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingProviderResponse> getAllProviders() {
        return providerRepository.findAll().stream()
            .map(providerMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShippingProviderResponse> getActiveProviders() {
        return providerRepository.findByIsActiveTrue().stream()
            .map(providerMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingProviderResponse getProviderById(Long id) {
        return providerMapper.toResponse(findById(id));
    }

    @Override
    public ShippingProviderResponse createProvider(ShippingProviderRequest request) {
        ShippingProvider provider = providerMapper.toEntity(request);
        provider.setIsActive(true);
        ShippingProvider saved = providerRepository.save(provider);
        log.info("Shipping provider created: {}", saved.getName());
        return providerMapper.toResponse(saved);
    }

    @Override
    public ShippingProviderResponse updateProvider(Long id, ShippingProviderRequest request) {
        ShippingProvider provider = findById(id);
        providerMapper.updateEntity(request, provider);
        ShippingProvider saved = providerRepository.save(provider);
        log.info("Shipping provider updated: {}", saved.getName());
        return providerMapper.toResponse(saved);
    }

    @Override
    public void deleteProvider(Long id) {
        ShippingProvider provider = findById(id);
        if (!provider.getIsActive()) {
            throw new BadRequestException("Provider is already inactive");
        }
        provider.setIsActive(false);
        providerRepository.save(provider);
        log.info("Shipping provider deactivated: {}", provider.getName());
    }

    private ShippingProvider findById(Long id) {
        return providerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipping provider not found with id: " + id));
    }
}
