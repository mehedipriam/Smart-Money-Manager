package com.smartmoneymanager.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.RecurringTransaction;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);

    /** Every active template due to fire, across all users — the scheduler's sweep query. */
    List<RecurringTransaction> findAllByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
