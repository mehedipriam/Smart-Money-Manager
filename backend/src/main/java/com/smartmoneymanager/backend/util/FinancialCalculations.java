package com.smartmoneymanager.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.smartmoneymanager.backend.dto.projection.TransactionAmountProjection;
import com.smartmoneymanager.backend.dto.response.CashFlowPointResponse;
import com.smartmoneymanager.backend.entity.enums.TransactionType;

/** Shared math behind the dashboard's and reports' period-based stats and charts. */
public final class FinancialCalculations {

    private FinancialCalculations() {
    }

    /** {@code null} means "no baseline to compare against" (frontend shows e.g. "New") rather than a misleading 0%/infinity%. */
    public static BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return current.subtract(previous)
                .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Buckets transactions by day (spans up to {@code dailyBucketMaxSpanDays})
     * or by month (longer spans), zero-filling every bucket in range so the
     * chart has no gaps for days/months with no activity.
     */
    public static List<CashFlowPointResponse> buildCashFlowBuckets(
            List<TransactionAmountProjection> transactions, LocalDate start, LocalDate end, int dailyBucketMaxSpanDays) {
        long spanDays = ChronoUnit.DAYS.between(start, end) + 1;
        boolean daily = spanDays <= dailyBucketMaxSpanDays;
        DateTimeFormatter bucketKeyFormatter = daily ? DateTimeFormatter.ISO_LOCAL_DATE : DateTimeFormatter.ofPattern("yyyy-MM");

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
