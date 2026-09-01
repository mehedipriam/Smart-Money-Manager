package com.smartmoneymanager.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.request.ChangePasswordRequest;
import com.smartmoneymanager.backend.dto.request.UpdateProfileRequest;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.AuthService;
import com.smartmoneymanager.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userService.getProfile(principal.getId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updated = userService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
