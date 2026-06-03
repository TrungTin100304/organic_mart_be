package com.bryan.service;

import com.bryan.dto.request.UpdateUserPreferenceRequest;
import com.bryan.dto.response.UserPreferenceResponse;

public interface UserPreferenceService {

    UserPreferenceResponse getCurrentUserPreference();

    UserPreferenceResponse getPreferenceById(Long id);

    UserPreferenceResponse createOrUpdatePreference(UpdateUserPreferenceRequest request);

    UserPreferenceResponse updatePreference(Long id, UpdateUserPreferenceRequest request);

    void deletePreference(Long id);
}
