package com.smartmoneymanager.backend.service;

import com.smartmoneymanager.backend.dto.request.UpdateProfileRequest;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
}
