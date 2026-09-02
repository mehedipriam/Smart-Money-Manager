package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.CreateBudgetRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBudgetRequest;
import com.smartmoneymanager.backend.dto.response.BudgetResponse;

public interface BudgetService {

    List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year);

    BudgetResponse createBudget(Long userId, CreateBudgetRequest request);

    BudgetResponse updateBudget(Long userId, Long budgetId, UpdateBudgetRequest request);

    void deleteBudget(Long userId, Long budgetId);
}
