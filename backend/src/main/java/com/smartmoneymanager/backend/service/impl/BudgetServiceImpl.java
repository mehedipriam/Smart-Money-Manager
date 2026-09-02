package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.CreateBudgetRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBudgetRequest;
import com.smartmoneymanager.backend.dto.response.BudgetResponse;
import com.smartmoneymanager.backend.entity.Budget;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.DuplicateResourceException;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.CategoryMapper;
import com.smartmoneymanager.backend.repository.BudgetRepository;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.BudgetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private static final BigDecimal NEAR_LIMIT_THRESHOLD = BigDecimal.valueOf(80);

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year) {
        LocalDate today = LocalDate.now();
        int resolvedMonth = month != null ? month : today.getMonthValue();
        int resolvedYear = year != null ? year : today.getYear();

        return budgetRepository.findAllByUserIdAndMonthAndYearOrderByCreatedAtDesc(userId, resolvedMonth, resolvedYear).stream()
                .map(budget -> toResponse(userId, budget))
                .toList();
    }

    @Override
    public BudgetResponse createBudget(Long userId, CreateBudgetRequest request) {
        Category category = findExpenseCategory(userId, request.getCategoryId());

        Budget budget = Budget.builder()
                .user(userRepository.getReferenceById(userId))
                .category(category)
                .budgetAmount(request.getBudgetAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .build();
        try {
            budget = budgetRepository.save(budget);
            budgetRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("A budget for this category already exists for that month");
        }
        return toResponse(userId, budget);
    }

    @Override
    public BudgetResponse updateBudget(Long userId, Long budgetId, UpdateBudgetRequest request) {
        Budget budget = findOwnedBudget(userId, budgetId);
        budget.setBudgetAmount(request.getBudgetAmount());
        return toResponse(userId, budgetRepository.save(budget));
    }

    @Override
    public void deleteBudget(Long userId, Long budgetId) {
        budgetRepository.delete(findOwnedBudget(userId, budgetId));
    }

    private BudgetResponse toResponse(Long userId, Budget budget) {
        YearMonth yearMonth = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal usedAmount = transactionRepository.sumAmountByCategory(
                userId, budget.getCategory().getId(), TransactionType.EXPENSE, start, end);
        BigDecimal remainingAmount = budget.getBudgetAmount().subtract(usedAmount);
        BigDecimal usagePercentage = budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0
                ? usedAmount.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(categoryMapper.toSummary(budget.getCategory()))
                .budgetAmount(budget.getBudgetAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .usedAmount(usedAmount)
                .remainingAmount(remainingAmount)
                .usagePercentage(usagePercentage)
                .nearLimit(usagePercentage.compareTo(NEAR_LIMIT_THRESHOLD) >= 0 && usagePercentage.compareTo(BigDecimal.valueOf(100)) < 0)
                .exceeded(usagePercentage.compareTo(BigDecimal.valueOf(100)) >= 0)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }

    private Category findExpenseCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean visible = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!visible) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidOperationException("Budgets can only be set on expense categories");
        }
        return category;
    }

    private Budget findOwnedBudget(Long userId, Long budgetId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }
}
