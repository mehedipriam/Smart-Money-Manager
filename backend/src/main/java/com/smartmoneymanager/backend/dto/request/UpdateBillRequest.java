package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.RecurringFrequency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Payment status is intentionally not editable here — it only moves through the dedicated "mark as paid" action, or the automatic PENDING-to-OVERDUE sweep. */
@Getter
@Setter
public class UpdateBillRequest {

    @NotBlank(message = "Bill name is required")
    @Size(max = 150, message = "Bill name must be at most 150 characters")
    private String billName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Amount has too many digits")
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private Long categoryId;

    private RecurringFrequency recurringType;
}
