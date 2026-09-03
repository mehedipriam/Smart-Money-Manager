package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.dto.response.CashFlowPointResponse;
import com.smartmoneymanager.backend.dto.response.DashboardResponse;
import com.smartmoneymanager.backend.dto.response.DashboardSummaryResponse;
import com.smartmoneymanager.backend.dto.response.SpendingByCategoryResponse;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.service.DashboardService;
import com.smartmoneymanager.backend.util.DateRangeUtils;
import com.smartmoneymanager.backend.util.FinancialCalculations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    /** Above this many days, the cash-flow chart buckets by month instead of by day (otherwise the chart would have too many points). */
    private static final int DAILY_BUCKET_MAX_SPAN_DAYS = 31;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public DashboardResponse getDashboard(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        LocalDate[] period = DateRangeUtils.resolveRange(range, customStart, customEnd, today);
        LocalDate periodStart = period[0];
        LocalDate periodEnd = period[1];

        long spanDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        LocalDate[] previousPeriod = DateRangeUtils.resolvePreviousRange(range, periodStart, periodEnd, spanDays);
        LocalDate prevStart = previousPeriod[0];
        LocalDate prevEnd = previousPeriod[1];

        BigDecimal totalBalance = accountRepository.sumCurrentBalance(userId);

        BigDecimal totalIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, periodStart, periodEnd);
        BigDecimal totalExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, periodStart, periodEnd);
        BigDecimal totalSavings = totalIncome.subtract(totalExpenses);

        LocalDate monthStart = today.withDayOfMonth(1);
        BigDecimal monthlyIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, monthStart, today);
        BigDecimal monthlyExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, monthStart, today);
        BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpenses);

        BigDecimal prevIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, prevStart, prevEnd);
        BigDecimal prevExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, prevStart, prevEnd);
        BigDecimal prevSavings = prevIncome.subtract(prevExpenses);

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalBalance(totalBalance)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .totalSavings(totalSavings)
                .monthlySavings(monthlySavings)
                .incomeChangePercent(FinancialCalculations.percentChange(totalIncome, prevIncome))
                .expenseChangePercent(FinancialCalculations.percentChange(totalExpenses, prevExpenses))
                .savingsChangePercent(FinancialCalculations.percentChange(totalSavings, prevSavings))
                .build();

        List<SpendingByCategoryResponse> spendingByCategory = buildSpendingByCategory(userId, periodStart, periodEnd, totalExpenses);
        List<CashFlowPointResponse> cashFlow = buildCashFlow(userId, periodStart, periodEnd);

        return DashboardResponse.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .summary(summary)
                .spendingByCategory(spendingByCategory)
                .cashFlow(cashFlow)
                .build();
    }

    private List<SpendingByCategoryResponse> buildSpendingByCategory(Long userId, LocalDate start, LocalDate end, BigDecimal totalExpenses) {
        List<SpendingByCategoryResponse> items = transactionRepository.findSpendingByCategory(userId, TransactionType.EXPENSE, start, end);
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            for (SpendingByCategoryResponse item : items) {
                item.setPercentage(item.getAmount()
                        .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
            }
        } else {
            items.forEach(item -> item.setPercentage(BigDecimal.ZERO));
        }
        return items;
    }

    private List<CashFlowPointResponse> buildCashFlow(Long userId, LocalDate start, LocalDate end) {
        return FinancialCalculations.buildCashFlowBuckets(
                transactionRepository.findAmountsInRange(userId, start, end), start, end, DAILY_BUCKET_MAX_SPAN_DAYS);
    }
}
