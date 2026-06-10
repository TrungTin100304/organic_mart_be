package com.bryan.mapper;

import com.bryan.entity.AddressLabel;
import com.bryan.entity.ResidentialBuilding;
import com.bryan.entity.UserAddress;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAddressMapperTest {

    private final UserAddressMapper mapper = Mappers.getMapper(UserAddressMapper.class);

    @Test
    void mapsSelectedBuildingToResponse() {
        ResidentialBuilding building = new ResidentialBuilding();
        building.setId(12L);
        building.setCode("S1.01");
        building.setName("Tòa S1.01 - The Rainbow");

        UserAddress address = new UserAddress();
        address.setLabel(AddressLabel.HOME);
        address.setRecipientName("Nguyen Van A");
        address.setRecipientPhone("0909000000");
        address.setFullAddress("");
        address.setBuilding(building);
        address.setFloor("5");
        address.setApartmentNumber("501");

        var response = mapper.toResponse(address);

        assertEquals(12L, response.buildingId());
        assertEquals("S1.01", response.buildingCode());
        assertEquals("Tòa S1.01 - The Rainbow", response.buildingName());
    }
}
