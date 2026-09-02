package com.smartmoneymanager.backend.mapper;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.RecurringTransactionResponse;
import com.smartmoneymanager.backend.entity.RecurringTransaction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecurringTransactionMapper {

    private final AccountMapper accountMapper;
    private final CategoryMapper categoryMapper;

    public RecurringTransactionResponse toResponse(RecurringTransaction recurring) {
        return RecurringTransactionResponse.builder()
                .id(recurring.getId())
                .account(accountMapper.toSummary(recurring.getAccount()))
                .category(categoryMapper.toSummary(recurring.getCategory()))
                .type(recurring.getType().name())
                .amount(recurring.getAmount())
                .description(recurring.getDescription())
                .note(recurring.getNote())
                .frequency(recurring.getFrequency().name())
                .startDate(recurring.getStartDate())
                .nextRunDate(recurring.getNextRunDate())
                .endDate(recurring.getEndDate())
                .active(recurring.isActive())
                .createdAt(recurring.getCreatedAt())
                .updatedAt(recurring.getUpdatedAt())
                .build();
    }
}
