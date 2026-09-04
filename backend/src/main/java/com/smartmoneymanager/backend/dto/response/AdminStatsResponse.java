package com.smartmoneymanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** System-wide counters for the admin dashboard. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long disabledUsers;
    private long verifiedUsers;
    private long newUsersThisMonth;
    private long totalTransactions;
    private long totalAccounts;
    private long totalBudgets;
    private long totalGoals;
    private long totalBills;
}
