package com.smartmoneymanager.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.GoalContribution;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {

    List<GoalContribution> findAllByGoalIdOrderByContributionDateDesc(Long goalId);
}
