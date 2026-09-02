package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.projection.TransactionAmountProjection;
import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.dto.response.CashFlowPointResponse;
import com.smartmoneymanager.backend.dto.response.DashboardResponse;
import com.smartmoneymanager.backend.dto.response.DashboardSummaryResponse;
import com.smartmoneymanager.backend.dto.response.SpendingByCategoryResponse;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.service.DashboardService;

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
        LocalDate[] period = resolveRange(range, customStart, customEnd, today);
        LocalDate periodStart = period[0];
        LocalDate periodEnd = period[1];

        long spanDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        LocalDate[] previousPeriod = resolvePreviousRange(range, periodStart, periodEnd, spanDays);
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
                .incomeChangePercent(percentChange(totalIncome, prevIncome))
                .expenseChangePercent(percentChange(totalExpenses, prevExpenses))
                .savingsChangePercent(percentChange(totalSavings, prevSavings))
                .build();

        List<SpendingByCategoryResponse> spendingByCategory = buildSpendingByCategory(userId, periodStart, periodEnd, totalExpenses);
        List<CashFlowPointResponse> cashFlow = buildCashFlow(userId, periodStart, periodEnd, spanDays);

        return DashboardResponse.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .summary(summary)
                .spendingByCategory(spendingByCategory)
                .cashFlow(cashFlow)
                .build();
    }

    private LocalDate[] resolveRange(DashboardRangeType range, LocalDate customStart, LocalDate customEnd, LocalDate today) {
        return switch (range) {
            case TODAY -> new LocalDate[] { today, today };
            case THIS_WEEK -> new LocalDate[] { today.minusDays(today.getDayOfWeek().getValue() - 1L), today };
            case THIS_MONTH -> new LocalDate[] { today.withDayOfMonth(1), today };
            case LAST_MONTH -> {
                LocalDate lastMonth = today.minusMonths(1);
                yield new LocalDate[] { lastMonth.withDayOfMonth(1), lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()) };
            }
            case THIS_YEAR -> new LocalDate[] { today.withDayOfYear(1), today };
            case CUSTOM -> {
                if (customStart == null || customEnd == null) {
                    throw new InvalidOperationException("startDate and endDate are required when range=CUSTOM");
                }
                if (customStart.isAfter(customEnd)) {
                    throw new InvalidOperationException("startDate cannot be after endDate");
                }
                yield new LocalDate[] { customStart, customEnd };
            }
        };
    }

    /**
     * The baseline period each stat's %-change is measured against. Deliberately
     * calendar-aware rather than a blind "immediately preceding N days": for
     * THIS_MONTH (month-to-date), comparing against the same day-of-month range
     * last month (e.g. Sep 1–3 vs Aug 1–3) is what "how am I trending this month"
     * actually means — the naive equal-length window ending the day before the
     * period start (Aug 29–31) would compare against an arbitrary few days that
     * have nothing to do with "last month." THIS_WEEK/THIS_YEAR follow the same
     * idea, one calendar unit back. Only CUSTOM (no named period to shift) and
     * LAST_MONTH (already a fixed month) fall back to the equal-length window.
     */
    private LocalDate[] resolvePreviousRange(DashboardRangeType range, LocalDate periodStart, LocalDate periodEnd, long spanDays) {
        return switch (range) {
            case TODAY -> new LocalDate[] { periodStart.minusDays(1), periodEnd.minusDays(1) };
            case THIS_WEEK -> new LocalDate[] { periodStart.minusWeeks(1), periodEnd.minusWeeks(1) };
            case THIS_MONTH -> new LocalDate[] { periodStart.minusMonths(1), periodEnd.minusMonths(1) };
            case THIS_YEAR -> new LocalDate[] { periodStart.minusYears(1), periodEnd.minusYears(1) };
            case LAST_MONTH, CUSTOM -> {
                LocalDate prevEnd = periodStart.minusDays(1);
                yield new LocalDate[] { prevEnd.minusDays(spanDays - 1), prevEnd };
            }
        };
    }

    /** {@code null} means "no baseline to compare against" (frontend shows e.g. "New") rather than a misleading 0%/∞%. */
    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return current.subtract(previous)
                .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<SpendingByCategoryResponse> buildSpendingByCategory(Long userId, LocalDate start, LocalDate end, BigDecimal totalExpenses) {
        List<SpendingByCategoryResponse> items = transactionRepository.findSpendingByCategory(userId, start, end);
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

    private List<CashFlowPointResponse> buildCashFlow(Long userId, LocalDate start, LocalDate end, long spanDays) {
        List<TransactionAmountProjection> transactions = transactionRepository.findAmountsInRange(userId, start, end);
        boolean daily = spanDays <= DAILY_BUCKET_MAX_SPAN_DAYS;
        DateTimeFormatter bucketKeyFormatter = daily ? DateTimeFormatter.ISO_LOCAL_DATE : DateTimeFormatter.ofPattern("yyyy-MM");

        // Pre-seed every bucket in the range at zero so the chart has no gaps for days/months with no activity.
        Map<String, BigDecimal[]> buckets = new LinkedHashMap<>(); // [income, expense]
        LocalDate cursor = daily ? start : start.withDayOfMonth(1);
        LocalDate boundary = daily ? end : end.withDayOfMonth(1);
        while (!cursor.isAfter(boundary)) {
            buckets.put(cursor.format(bucketKeyFormatter), new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            cursor = daily ? cursor.plusDays(1) : cursor.plusMonths(1);
        }

        for (TransactionAmountProjection t : transactions) {
            String key = t.transactionDate().format(bucketKeyFormatter);
            BigDecimal[] bucket = buckets.get(key);
            if (bucket == null) {
                continue;
            }
            int index = t.type() == TransactionType.INCOME ? 0 : 1;
            bucket[index] = bucket[index].add(t.amount());
        }

        List<CashFlowPointResponse> points = new ArrayList<>(buckets.size());
        buckets.forEach((label, amounts) -> points.add(CashFlowPointResponse.builder()
                .label(label)
                .income(amounts[0])
                .expense(amounts[1])
                .savings(amounts[0].subtract(amounts[1]))
                .build()));
        return points;
    }
}
