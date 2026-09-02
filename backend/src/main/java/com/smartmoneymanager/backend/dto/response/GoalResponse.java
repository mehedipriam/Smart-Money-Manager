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
public class GoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentSavedAmount;
    private LocalDate targetDate;
    private String description;
    private String status;
    private BigDecimal progressPercentage;
    private BigDecimal remainingAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
