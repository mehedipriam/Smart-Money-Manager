package com.smartmoneymanager.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.request.AddGoalContributionRequest;
import com.smartmoneymanager.backend.dto.request.CreateGoalRequest;
import com.smartmoneymanager.backend.dto.request.UpdateGoalRequest;
import com.smartmoneymanager.backend.dto.response.GoalContributionResponse;
import com.smartmoneymanager.backend.dto.response.GoalResponse;
import com.smartmoneymanager.backend.entity.enums.GoalStatus;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.GoalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) GoalStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Goals fetched", goalService.getGoals(principal.getId(), status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Goal fetched", goalService.getGoal(principal.getId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateGoalRequest request) {
        GoalResponse created = goalService.createGoal(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Goal created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalResponse updated = goalService.updateGoal(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Goal updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        goalService.deleteGoal(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted"));
    }

    @PostMapping("/{id}/contributions")
    public ResponseEntity<ApiResponse<GoalResponse>> addContribution(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AddGoalContributionRequest request) {
        GoalResponse updated = goalService.addContribution(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Money added to goal", updated));
    }

    @GetMapping("/{id}/contributions")
    public ResponseEntity<ApiResponse<List<GoalContributionResponse>>> listContributions(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Contributions fetched", goalService.getContributions(principal.getId(), id)));
    }
}
