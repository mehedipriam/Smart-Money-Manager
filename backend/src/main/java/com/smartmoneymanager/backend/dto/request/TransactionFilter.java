package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.TransactionType;

/** Plain carrier for the optional query-string filters on GET /api/transactions — built by the controller, not bound/validated from a JSON body. */
public record TransactionFilter(
        Long accountId,
        Long categoryId,
        TransactionType type,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String search) {
}
