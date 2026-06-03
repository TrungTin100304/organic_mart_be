package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.bryan.entity.AddressLabel;

public record UserAddressRequest(
    @NotNull(message = "Nhãn là bắt buộc")
    AddressLabel label,

    @Size(max = 100, message = "Nhãn tùy chỉnh không được vượt quá 100 ký tự")
    String customLabel,

    @NotBlank(message = "Tên người nhận là bắt buộc")
    @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
    String recipientName,

    @NotBlank(message = "Số điện thoại người nhận là bắt buộc")
    @Size(max = 20, message = "Số điện thoại người nhận không được vượt quá 20 ký tự")
    String recipientPhone,

    @NotBlank(message = "Địa chỉ đầy đủ là bắt buộc")
    String fullAddress,

    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    String ward,

    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    String district,

    @Size(max = 100, message = "Thành phố/Tỉnh không được vượt quá 100 ký tự")
    String city,

    boolean isDefault
) {}
