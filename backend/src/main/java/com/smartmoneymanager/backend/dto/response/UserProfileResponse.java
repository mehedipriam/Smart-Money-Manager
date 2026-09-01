package com.smartmoneymanager.backend.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String defaultCurrency;
    private String preferredLanguage;
    private boolean emailVerified;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
