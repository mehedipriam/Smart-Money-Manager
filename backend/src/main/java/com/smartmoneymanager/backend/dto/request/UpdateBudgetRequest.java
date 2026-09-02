package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Category and month/year are fixed at creation — to move a budget to a different period, delete and recreate it. */
@Getter
@Setter
public class UpdateBudgetRequest {

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Budget amount has too many digits")
    private BigDecimal budgetAmount;
}
