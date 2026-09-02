package com.smartmoneymanager.backend.mapper;

import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.dto.response.BillResponse;
import com.smartmoneymanager.backend.entity.Bill;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BillMapper {

    private final CategoryMapper categoryMapper;

    public BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billName(bill.getBillName())
                .amount(bill.getAmount())
                .dueDate(bill.getDueDate())
                .category(bill.getCategory() != null ? categoryMapper.toSummary(bill.getCategory()) : null)
                .recurringType(bill.getRecurringType() != null ? bill.getRecurringType().name() : null)
                .paymentStatus(bill.getPaymentStatus().name())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
