package com.smartmoneymanager.backend.mapper;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.TransactionResponse;
import com.smartmoneymanager.backend.entity.Transaction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .account(accountMapper.toSummary(transaction.getAccount()))
                .category(categoryMapper.toSummary(transaction.getCategory()))
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .note(transaction.getNote())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
