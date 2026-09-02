package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    /** Current sum of all account balances — a snapshot, not scoped to the selected period. */
    private BigDecimal totalBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalSavings;
    /** Net savings for the current calendar month specifically, regardless of the selected period. */
    private BigDecimal monthlySavings;
    /** Percent change vs. the immediately preceding period of the same length. Null when the previous period had no income to compare against. */
    private BigDecimal incomeChangePercent;
    private BigDecimal expenseChangePercent;
    private BigDecimal savingsChangePercent;
}
