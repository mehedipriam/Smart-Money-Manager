package com.smartmoneymanager.backend.dto.request;

/** Named quick-filters for the dashboard date range; CUSTOM requires explicit startDate/endDate query params. */
public enum DashboardRangeType {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM
}
