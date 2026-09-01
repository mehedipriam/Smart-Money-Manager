package com.smartmoneymanager.backend.dto.request;

import com.smartmoneymanager.backend.entity.enums.Currency;
import com.smartmoneymanager.backend.entity.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;

    @Size(max = 500, message = "Profile image URL must be at most 500 characters")
    private String profileImageUrl;

    @NotNull(message = "Default currency is required")
    private Currency defaultCurrency;

    @NotNull(message = "Preferred language is required")
    private Language preferredLanguage;
}
