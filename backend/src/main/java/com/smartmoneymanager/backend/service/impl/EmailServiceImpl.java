package com.smartmoneymanager.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends transactional emails. Sending is best-effort: a local dev
 * environment often has no SMTP server configured, so a failure here is
 * logged (with the link, for manual testing) rather than propagated —
 * it must never block registration or password-reset requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        log.info("Email verification link for {}: {}", user.getEmail(), link);
        send(user.getEmail(), "Verify your Smart Money Manager account",
                "Hi " + user.getFullName() + ",\n\n"
                        + "Welcome to Smart Money Manager! Please verify your email address by opening the link below:\n\n"
                        + link + "\n\n"
                        + "This link expires in 24 hours. If you did not create this account, you can ignore this email.");
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        log.info("Password reset link for {}: {}", user.getEmail(), link);
        send(user.getEmail(), "Reset your Smart Money Manager password",
                "Hi " + user.getFullName() + ",\n\n"
                        + "We received a request to reset your password. Open the link below to choose a new one:\n\n"
                        + link + "\n\n"
                        + "This link expires in 1 hour. If you did not request this, you can safely ignore this email.");
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {} ({}). Continuing without failing the request.", to, e.getMessage());
        }
    }
}
