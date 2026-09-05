package com.smartmoneymanager.backend.service.importing;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.TransactionType;

/**
 * A single transaction candidate produced by an {@link ImportParser}, already mapped away from
 * whatever source-specific columns it came from. Nothing downstream of this (duplicate detection,
 * category suggestion, preview response, eventual creation via the normal transaction API) knows
 * or cares which parser produced it.
 */
public record NormalizedTransactionRow(
        LocalDate transactionDate,
        String description,
        TransactionType type,
        BigDecimal amount,
        String currency,
        String externalReference) {
}
