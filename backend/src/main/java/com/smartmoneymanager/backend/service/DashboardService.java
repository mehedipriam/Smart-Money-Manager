package com.smartmoneymanager.backend.service;

import java.time.LocalDate;

import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd);
}
