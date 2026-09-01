package com.smartmoneymanager.backend.service;

import com.smartmoneymanager.backend.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);
}
