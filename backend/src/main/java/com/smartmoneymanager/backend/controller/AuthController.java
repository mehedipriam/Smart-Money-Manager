package com.smartmoneymanager.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.request.ForgotPasswordRequest;
import com.smartmoneymanager.backend.dto.request.LoginRequest;
import com.smartmoneymanager.backend.dto.request.RefreshTokenRequest;
import com.smartmoneymanager.backend.dto.request.RegisterRequest;
import com.smartmoneymanager.backend.dto.request.ResendVerificationRequest;
import com.smartmoneymanager.backend.dto.request.ResetPasswordRequest;
import com.smartmoneymanager.backend.dto.response.AuthResponse;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;
import com.smartmoneymanager.backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse profile = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Registration successful. Please check your email to verify your account.", profile));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Stateless JWT: there is nothing to invalidate server-side. The client is
        // responsible for discarding its access/refresh tokens.
        return ResponseEntity.ok(ApiResponse.success("Logged out"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a verification link has been sent."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If an account with that email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now log in with your new password."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }
}
