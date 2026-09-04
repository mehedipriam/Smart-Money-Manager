package com.smartmoneymanager.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    /** Most recently issued token for a user — used by tests to verify an account without a real mailbox. */
    Optional<EmailVerificationToken> findFirstByUserIdOrderByIdDesc(Long userId);
}
