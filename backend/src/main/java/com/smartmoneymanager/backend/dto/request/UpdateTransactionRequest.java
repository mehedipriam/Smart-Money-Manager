package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartmoneymanager.backend.entity.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionRequest {

    @NotNull(message = "Account is required")
    private Long accountId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4, message = "Amount has too many digits")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @Size(max = 1000, message = "Note must be at most 1000 characters")
    private String note;
}
