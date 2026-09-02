package com.smartmoneymanager.backend.mapper;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.AccountResponse;
import com.smartmoneymanager.backend.dto.response.AccountSummaryResponse;
import com.smartmoneymanager.backend.entity.Account;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType().name())
                .initialBalance(account.getInitialBalance())
                .currentBalance(account.getCurrentBalance())
                .currency(account.getCurrency().name())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public AccountSummaryResponse toSummary(Account account) {
        return AccountSummaryResponse.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .currency(account.getCurrency().name())
                .build();
    }
}
