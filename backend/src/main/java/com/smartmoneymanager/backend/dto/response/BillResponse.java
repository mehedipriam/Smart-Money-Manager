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
public class BillResponse {

    private Long id;
    private String billName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private CategorySummaryResponse category;
    private String recurringType;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
