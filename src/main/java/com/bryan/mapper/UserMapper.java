package com.bryan.mapper;

import com.bryan.dto.request.UserUpdateRequest;
import com.bryan.dto.response.UserResponse;
import com.bryan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "active", target = "isActive")
    @Mapping(target = "heightCm", expression = "java(UserMapperHelper.getHeight(user))")
    @Mapping(target = "weightKg", expression = "java(UserMapperHelper.getWeight(user))")
    @Mapping(target = "bmi", expression = "java(UserMapperHelper.calculateBmi(user))")
    @Mapping(target = "healthGoal", expression = "java(UserMapperHelper.getHealthGoal(user))")
    @Mapping(target = "dietType", expression = "java(UserMapperHelper.getDietType(user))")
    @Mapping(target = "dailyCalorieTarget", expression = "java(UserMapperHelper.getDailyCalorieTarget(user))")
    UserResponse toResponse(User user);

    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);
}
