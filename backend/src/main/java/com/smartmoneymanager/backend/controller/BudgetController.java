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
import com.smartmoneymanager.backend.dto.request.CreateBudgetRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBudgetRequest;
import com.smartmoneymanager.backend.dto.response.BudgetResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.BudgetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success("Budgets fetched", budgetService.getBudgets(principal.getId(), month, year)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse created = budgetService.createBudget(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Budget created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        BudgetResponse updated = budgetService.updateBudget(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        budgetService.deleteBudget(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted"));
    }
}
