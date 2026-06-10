package com.bryan.dto.request;

import com.bryan.entity.AddressLabel;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAddressRequestValidationTest {

    @Test
    void allowsBlankFullAddressForInternalDelivery() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var request = new UserAddressRequest(
            AddressLabel.HOME,
            null,
            "Nguyen Van A",
            "0909000000",
            "",
            "",
            "",
            "",
            true,
            1L,
            "5",
            "501",
            null
        );

        var fullAddressViolations = validator.validate(request).stream()
            .filter(violation -> violation.getPropertyPath().toString().equals("fullAddress"))
            .toList();

        assertTrue(fullAddressViolations.isEmpty());
    }
}
