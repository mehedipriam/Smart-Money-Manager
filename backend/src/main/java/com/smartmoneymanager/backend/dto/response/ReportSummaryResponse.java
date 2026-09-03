package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Aggregated financial report for a period: the analytics figures and chart
 * data behind the Reports page's Monthly/Yearly/Income/Expense/Savings/Category views.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    /** Percentage of income kept as savings (netSavings / totalIncome * 100). Null when there was no income in the period. */
    private BigDecimal savingsRate;
    /** Percent change vs. the equivalent preceding period. Null when the previous period had nothing to compare against. */
    private BigDecimal incomeChangePercent;
    private BigDecimal expenseChangePercent;
    private BigDecimal savingsChangePercent;

    /** Expense category with the highest spend in the period, or null if there were no expenses. */
    private SpendingByCategoryResponse highestExpenseCategory;
    /** Total expenses spread evenly across the number of calendar months the period spans (minimum 1). */
    private BigDecimal averageMonthlyExpense;

    private List<SpendingByCategoryResponse> expenseByCategory;
    private List<SpendingByCategoryResponse> incomeByCategory;

    /** Income/expense/savings bucketed by day or month depending on the selected period's span. */
    private List<CashFlowPointResponse> cashFlow;
    /** Fixed trailing 6 calendar months of income/expense/savings, independent of the selected range - the basis for the Monthly Cash Flow and Savings Trend charts. */
    private List<CashFlowPointResponse> monthlyTrend;
}
