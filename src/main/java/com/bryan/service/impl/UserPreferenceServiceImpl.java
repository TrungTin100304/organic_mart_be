package com.bryan.service.impl;

import com.bryan.dto.request.UpdateUserPreferenceRequest;
import com.bryan.dto.response.UserPreferenceResponse;
import com.bryan.entity.User;
import com.bryan.entity.UserPreference;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.UserPreferenceMapper;
import com.bryan.repository.UserPreferenceRepository;
import com.bryan.repository.UserRepository;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final UserPreferenceMapper userPreferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getCurrentUserPreference() {
        Long userId = getCurrentUserId();
        return userPreferenceMapper.toResponse(getPreferenceEntityByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferenceById(Long id) {
        return userPreferenceMapper.toResponse(getPreferenceEntityById(id));
    }

    @Override
    public UserPreferenceResponse createOrUpdatePreference(UpdateUserPreferenceRequest request) {
        Long userId = getCurrentUserId();
        User user = findUserById(userId);

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreference newPref = new UserPreference();
                    newPref.setUser(user);
                    return newPref;
                });

        userPreferenceMapper.updateEntity(request, preference);
        recalculateBmi(preference);
        UserPreference saved = userPreferenceRepository.save(preference);
        return userPreferenceMapper.toResponse(saved);
    }

    @Override
    public UserPreferenceResponse updatePreference(Long id, UpdateUserPreferenceRequest request) {
        UserPreference preference = getPreferenceEntityById(id);
        userPreferenceMapper.updateEntity(request, preference);
        recalculateBmi(preference);
        UserPreference saved = userPreferenceRepository.save(preference);
        return userPreferenceMapper.toResponse(saved);
    }

    @Override
    public void deletePreference(Long id) {
        if (!userPreferenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("UserPreference not found with id: " + id);
        }
        userPreferenceRepository.deleteById(id);
    }

    private UserPreference getPreferenceEntityById(Long id) {
        return userPreferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreference not found with id: " + id));
    }

    private UserPreference getPreferenceEntityByUserId(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserPreference not found for current user"));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    private void recalculateBmi(UserPreference preference) {
        if (preference.getHeightCm() != null && preference.getWeightKg() != null
                && preference.getHeightCm().compareTo(java.math.BigDecimal.ZERO) > 0) {
            java.math.BigDecimal heightM = preference.getHeightCm()
                    .divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            preference.setBmi(preference.getWeightKg()
                    .divide(heightM.multiply(heightM), 2, java.math.RoundingMode.HALF_UP));
        }
    }
}
