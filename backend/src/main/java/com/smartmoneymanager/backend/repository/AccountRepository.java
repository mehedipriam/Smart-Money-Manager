package com.smartmoneymanager.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /** Ownership-scoped lookup — every account read/write must go through this, never a bare findById. */
    Optional<Account> findByIdAndUserId(Long id, Long userId);
}
