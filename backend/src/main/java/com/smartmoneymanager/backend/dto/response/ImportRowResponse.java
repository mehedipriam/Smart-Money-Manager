package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.TransactionType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImportRowResponse {
    private final LocalDate transactionDate;
    private final String description;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String currency;
    private final String externalReference;
    /** True if the user already has a transaction that looks like this one (same date/amount/type, matching reference). */
    private final boolean duplicate;
}
