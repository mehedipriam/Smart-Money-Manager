package com.smartmoneymanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Lightweight account reference embedded in transaction/recurring-transaction responses. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

    private Long id;
    private String accountName;
    private String currency;
}
