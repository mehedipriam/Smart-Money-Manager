package com.smartmoneymanager.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByUserIdAndMonthAndYearOrderByCreatedAtDesc(Long userId, Integer month, Integer year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);
}
