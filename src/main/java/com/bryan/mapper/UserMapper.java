package com.bryan.mapper;

import com.bryan.dto.request.UserUpdateRequest;
import com.bryan.dto.response.UserResponse;
import com.bryan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "avatarUrl", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);
}
