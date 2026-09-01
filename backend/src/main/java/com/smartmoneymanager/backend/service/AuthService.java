package com.smartmoneymanager.backend.service;

import com.smartmoneymanager.backend.dto.request.ChangePasswordRequest;
import com.smartmoneymanager.backend.dto.request.LoginRequest;
import com.smartmoneymanager.backend.dto.request.RegisterRequest;
import com.smartmoneymanager.backend.dto.response.AuthResponse;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;

public interface AuthService {

    UserProfileResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    AuthResponse refreshToken(String refreshToken);

    void changePassword(Long userId, ChangePasswordRequest request);
}
