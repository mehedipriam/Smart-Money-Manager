package com.smartmoneymanager.backend.dto.request;

import com.smartmoneymanager.backend.entity.enums.AccountType;
import com.smartmoneymanager.backend.entity.enums.Currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Balances are intentionally not editable here — they only move through
 * deposits/withdrawals/transfers (transaction-backed), never a direct edit,
 * so the balance can't silently drift from its transaction history.
 */
@Getter
@Setter
public class UpdateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must be at most 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private Currency currency;
}
