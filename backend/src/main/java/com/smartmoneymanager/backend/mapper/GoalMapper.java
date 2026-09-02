package com.smartmoneymanager.backend.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.GoalContributionResponse;
import com.smartmoneymanager.backend.dto.response.GoalResponse;
import com.smartmoneymanager.backend.entity.Goal;
import com.smartmoneymanager.backend.entity.GoalContribution;

@Component
public class GoalMapper {

    public GoalResponse toResponse(Goal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentSavedAmount());
        BigDecimal progress = goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0
                ? goal.getCurrentSavedAmount()
                        .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentSavedAmount(goal.getCurrentSavedAmount())
                .targetDate(goal.getTargetDate())
                .description(goal.getDescription())
                .status(goal.getStatus().name())
                .progressPercentage(progress)
                .remainingAmount(remaining)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    public GoalContributionResponse toContributionResponse(GoalContribution contribution) {
        return GoalContributionResponse.builder()
                .id(contribution.getId())
                .amount(contribution.getAmount())
                .contributionDate(contribution.getContributionDate())
                .note(contribution.getNote())
                .createdAt(contribution.getCreatedAt())
                .build();
    }
}
