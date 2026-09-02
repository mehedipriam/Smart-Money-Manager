package com.smartmoneymanager.backend.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.TransactionType;

/** Lightweight JPQL constructor-expression projection used only to bucket transactions for the dashboard cash-flow chart. */
public record TransactionAmountProjection(TransactionType type, BigDecimal amount, LocalDate transactionDate) {
}
