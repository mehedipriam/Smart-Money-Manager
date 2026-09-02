package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.AddGoalContributionRequest;
import com.smartmoneymanager.backend.dto.request.CreateGoalRequest;
import com.smartmoneymanager.backend.dto.request.UpdateGoalRequest;
import com.smartmoneymanager.backend.dto.response.GoalContributionResponse;
import com.smartmoneymanager.backend.dto.response.GoalResponse;
import com.smartmoneymanager.backend.entity.enums.GoalStatus;

public interface GoalService {

    List<GoalResponse> getGoals(Long userId, GoalStatus status);

    GoalResponse getGoal(Long userId, Long goalId);

    GoalResponse createGoal(Long userId, CreateGoalRequest request);

    GoalResponse updateGoal(Long userId, Long goalId, UpdateGoalRequest request);

    void deleteGoal(Long userId, Long goalId);

    GoalResponse addContribution(Long userId, Long goalId, AddGoalContributionRequest request);

    List<GoalContributionResponse> getContributions(Long userId, Long goalId);
}
