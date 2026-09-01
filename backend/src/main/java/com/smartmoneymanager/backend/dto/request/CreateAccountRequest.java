package com.smartmoneymanager.backend.dto.request;

import java.math.BigDecimal;

import com.smartmoneymanager.backend.entity.enums.AccountType;
import com.smartmoneymanager.backend.entity.enums.Currency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must be at most 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    @Digits(integer = 15, fraction = 4, message = "Initial balance has too many digits")
    private BigDecimal initialBalance;

    @NotNull(message = "Currency is required")
    private Currency currency;
}
