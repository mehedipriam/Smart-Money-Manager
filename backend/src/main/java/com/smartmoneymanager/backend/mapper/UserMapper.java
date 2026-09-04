package com.smartmoneymanager.backend.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.AdminUserResponse;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;
import com.smartmoneymanager.backend.entity.User;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .defaultCurrency(user.getDefaultCurrency().name())
                .preferredLanguage(user.getPreferredLanguage().name())
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
