package com.smartmoneymanager.backend.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.AddGoalContributionRequest;
import com.smartmoneymanager.backend.dto.request.CreateGoalRequest;
import com.smartmoneymanager.backend.dto.request.UpdateGoalRequest;
import com.smartmoneymanager.backend.dto.response.GoalContributionResponse;
import com.smartmoneymanager.backend.dto.response.GoalResponse;
import com.smartmoneymanager.backend.entity.Goal;
import com.smartmoneymanager.backend.entity.GoalContribution;
import com.smartmoneymanager.backend.entity.enums.GoalStatus;
import com.smartmoneymanager.backend.entity.enums.NotificationType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.GoalMapper;
import com.smartmoneymanager.backend.repository.GoalContributionRepository;
import com.smartmoneymanager.backend.repository.GoalRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.GoalService;
import com.smartmoneymanager.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final GoalMapper goalMapper;

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId, GoalStatus status) {
        List<Goal> goals = status == null
                ? goalRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                : goalRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return goals.stream().map(goalMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long userId, Long goalId) {
        return goalMapper.toResponse(findOwnedGoal(userId, goalId));
    }

    @Override
    public GoalResponse createGoal(Long userId, CreateGoalRequest request) {
        Goal goal = Goal.builder()
                .user(userRepository.getReferenceById(userId))
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .description(request.getDescription())
                .status(GoalStatus.ACTIVE)
                .build();
        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    public GoalResponse updateGoal(Long userId, Long goalId, UpdateGoalRequest request) {
        Goal goal = findOwnedGoal(userId, goalId);
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setDescription(request.getDescription());
        goal.setStatus(request.getStatus());
        applyAutoCompletion(goal);
        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    public void deleteGoal(Long userId, Long goalId) {
        goalRepository.delete(findOwnedGoal(userId, goalId));
    }

    @Override
    public GoalResponse addContribution(Long userId, Long goalId, AddGoalContributionRequest request) {
        Goal goal = findOwnedGoal(userId, goalId);
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot add money to a goal that is not active");
        }

        GoalContribution contribution = GoalContribution.builder()
                .goal(goal)
                .amount(request.getAmount())
                .contributionDate(request.getContributionDate() != null ? request.getContributionDate() : LocalDate.now())
                .note(request.getNote())
                .build();
        goalContributionRepository.save(contribution);

        goal.setCurrentSavedAmount(goal.getCurrentSavedAmount().add(request.getAmount()));
        applyAutoCompletion(goal);

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getContributions(Long userId, Long goalId) {
        findOwnedGoal(userId, goalId); // ownership check
        return goalContributionRepository.findAllByGoalIdOrderByContributionDateDesc(goalId).stream()
                .map(goalMapper::toContributionResponse)
                .toList();
    }

    /** Reaching or passing the target amount while ACTIVE auto-completes the goal — checked after both contributions and target-amount edits. */
    private void applyAutoCompletion(Goal goal) {
        if (goal.getStatus() == GoalStatus.ACTIVE && goal.getCurrentSavedAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
            notificationService.notify(goal.getUser().getId(), NotificationType.GOAL_COMPLETED, "Goal completed",
                    "You've reached your \"" + goal.getGoalName() + "\" goal!");
        }
    }

    private Goal findOwnedGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }
}
