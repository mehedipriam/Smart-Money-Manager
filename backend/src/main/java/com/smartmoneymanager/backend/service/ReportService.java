package com.smartmoneymanager.backend.service;

import java.time.LocalDate;

import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.dto.response.ReportSummaryResponse;

public interface ReportService {

    ReportSummaryResponse getReportSummary(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd);

    /** CSV bytes: a summary header followed by the raw transaction list for the period. */
    byte[] exportCsv(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd);

    /** PDF bytes: a formatted summary + category breakdown report for the period. */
    byte[] exportPdf(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd);
}
