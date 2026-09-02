package com.smartmoneymanager.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmoneymanager.backend.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /** Ownership-scoped lookup — every account read/write must go through this, never a bare findById. */
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    /**
     * Naive sum across all of a user's accounts, regardless of currency — the
     * spec explicitly rules out auto currency conversion without an exchange
     * rate API, so this is only meaningful when every account shares one
     * currency. Fine as the dashboard's "Total Balance" for now; flagged here
     * as the known simplification it is.
     */
    @Query("SELECT COALESCE(SUM(a.currentBalance), 0) FROM Account a WHERE a.user.id = :userId")
    BigDecimal sumCurrentBalance(@Param("userId") Long userId);
}
