package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private AccountSummaryResponse account;
    private CategorySummaryResponse category;
    private String type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
