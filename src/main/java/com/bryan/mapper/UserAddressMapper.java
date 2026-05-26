package com.bryan.mapper;

import com.bryan.dto.request.UserAddressRequest;
import com.bryan.dto.response.UserAddressResponse;
import com.bryan.entity.UserAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {

    UserAddressResponse toResponse(UserAddress userAddress);

    List<UserAddressResponse> toResponseList(List<UserAddress> userAddresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserAddress toEntity(UserAddressRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UserAddressRequest request, @MappingTarget UserAddress userAddress);
}
