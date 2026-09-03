package com.smartmoneymanager.backend.util;

import java.time.LocalDate;

import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;

/**
 * Resolves a {@link DashboardRangeType} into concrete date bounds, and the
 * equivalent prior period used for %-change comparisons. Shared by the
 * dashboard and reports modules, which both filter by the same named periods.
 */
public final class DateRangeUtils {

    private DateRangeUtils() {
    }

    public static LocalDate[] resolveRange(DashboardRangeType range, LocalDate customStart, LocalDate customEnd, LocalDate today) {
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
     * last month (e.g. Sep 1-3 vs Aug 1-3) is what "how am I trending this month"
     * actually means - the naive equal-length window ending the day before the
     * period start (Aug 29-31) would compare against an arbitrary few days that
     * have nothing to do with "last month." THIS_WEEK/THIS_YEAR follow the same
     * idea, one calendar unit back. Only CUSTOM (no named period to shift) and
     * LAST_MONTH (already a fixed month) fall back to the equal-length window.
     */
    public static LocalDate[] resolvePreviousRange(DashboardRangeType range, LocalDate periodStart, LocalDate periodEnd, long spanDays) {
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
}
