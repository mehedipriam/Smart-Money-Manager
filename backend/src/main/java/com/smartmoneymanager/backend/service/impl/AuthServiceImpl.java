package com.smartmoneymanager.backend.service.impl;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.ChangePasswordRequest;
import com.smartmoneymanager.backend.dto.request.LoginRequest;
import com.smartmoneymanager.backend.dto.request.RegisterRequest;
import com.smartmoneymanager.backend.dto.response.AuthResponse;
import com.smartmoneymanager.backend.dto.response.UserProfileResponse;
import com.smartmoneymanager.backend.entity.EmailVerificationToken;
import com.smartmoneymanager.backend.entity.PasswordResetToken;
import com.smartmoneymanager.backend.entity.Role;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.entity.enums.RoleName;
import com.smartmoneymanager.backend.exception.DuplicateResourceException;
import com.smartmoneymanager.backend.exception.EmailNotVerifiedException;
import com.smartmoneymanager.backend.exception.InvalidCredentialsException;
import com.smartmoneymanager.backend.exception.InvalidTokenException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.UserMapper;
import com.smartmoneymanager.backend.repository.EmailVerificationTokenRepository;
import com.smartmoneymanager.backend.repository.PasswordResetTokenRepository;
import com.smartmoneymanager.backend.repository.RoleRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.security.JwtTokenProvider;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.AuthService;
import com.smartmoneymanager.backend.service.EmailService;
import com.smartmoneymanager.backend.util.TokenGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final long EMAIL_VERIFICATION_VALIDITY_HOURS = 24;
    private static final long PASSWORD_RESET_VALIDITY_MINUTES = 60;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Override
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER is not seeded"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        issueEmailVerificationToken(user);

        return userMapper.toProfileResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        return buildAuthResponse(user);
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification link"));

        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("This verification link has already been used");
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("This verification link has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                issueEmailVerificationToken(user);
            }
        });
        // Silent no-op if the email is unknown or already verified, so this endpoint never reveals account existence.
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = TokenGenerator.generate();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiryDate(LocalDateTime.now().plusMinutes(PASSWORD_RESET_VALIDITY_MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user, token);
        });
        // Silent no-op if the email is unknown, so this endpoint never reveals account existence.
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset link"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This reset link has already been used");
        }
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("This reset link has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!user.isEnabled()) {
            throw new InvalidTokenException("This account has been disabled");
        }

        return buildAuthResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private void issueEmailVerificationToken(User user) {
        String token = TokenGenerator.generate();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_VALIDITY_HOURS))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user, token);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.of(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpirationMs())
                .user(userMapper.toProfileResponse(user))
                .build();
    }
}
