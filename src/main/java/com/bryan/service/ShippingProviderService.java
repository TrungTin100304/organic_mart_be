package com.bryan.service;

import com.bryan.dto.request.ShippingProviderRequest;
import com.bryan.dto.response.ShippingProviderResponse;

import java.util.List;

public interface ShippingProviderService {

    List<ShippingProviderResponse> getAllProviders();

    List<ShippingProviderResponse> getActiveProviders();

    ShippingProviderResponse getProviderById(Long id);

    ShippingProviderResponse createProvider(ShippingProviderRequest request);

    ShippingProviderResponse updateProvider(Long id, ShippingProviderRequest request);

    void deleteProvider(Long id);
}
