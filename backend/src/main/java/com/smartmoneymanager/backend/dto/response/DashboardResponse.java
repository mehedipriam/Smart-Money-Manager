package com.smartmoneymanager.backend.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private DashboardSummaryResponse summary;
    private List<SpendingByCategoryResponse> spendingByCategory;
    private List<CashFlowPointResponse> cashFlow;
}
