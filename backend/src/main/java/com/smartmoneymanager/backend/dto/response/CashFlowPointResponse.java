package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One bucket (day or month, depending on the selected range's span) of the cash-flow chart. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowPointResponse {

    /** e.g. "2026-08-05" for a daily bucket, or "2026-08" for a monthly bucket. */
    private String label;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal savings;
}
