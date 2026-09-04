package com.smartmoneymanager.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.smartmoneymanager.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByEnabledTrue();

    long countByEnabledFalse();

    long countByEmailVerifiedTrue();

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);
}
