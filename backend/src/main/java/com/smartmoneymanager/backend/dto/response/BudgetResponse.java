package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * {@code nearLimit} (>=80% used) and {@code exceeded} (>100% used) stand in for
 * the spec's "notify at 80% / exceeded" alerts — there's no Notification
 * system to push them through until Phase 12, so the frontend surfaces these
 * flags visually (badge/toast) instead.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private Long id;
    private CategorySummaryResponse category;
    private BigDecimal budgetAmount;
    private Integer month;
    private Integer year;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private BigDecimal usagePercentage;
    private boolean nearLimit;
    private boolean exceeded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
