package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountName;
    private String accountType;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
