package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 150, message = "Goal name must be at most 150 characters")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Target amount has too many digits")
    private BigDecimal targetAmount;

    /** Optional — a goal doesn't have to have a deadline. */
    private LocalDate targetDate;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
}
