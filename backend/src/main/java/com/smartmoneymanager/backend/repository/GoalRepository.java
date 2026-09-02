package com.smartmoneymanager.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.Goal;
import com.smartmoneymanager.backend.entity.enums.GoalStatus;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findAllByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);
}
