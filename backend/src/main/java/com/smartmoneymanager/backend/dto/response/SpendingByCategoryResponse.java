package com.smartmoneymanager.backend.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * One slice of the dashboard's category-wise spending breakdown.
 * The 5-arg constructor is used directly as a JPQL constructor expression
 * (see {@code TransactionRepository.findSpendingByCategory}); {@code percentage}
 * is filled in afterwards in the service layer, once the period total is known.
 */
@Getter
@Setter
public class SpendingByCategoryResponse {

    private final Long categoryId;
    private final String categoryName;
    private final String icon;
    private final String color;
    private final BigDecimal amount;
    private BigDecimal percentage;

    public SpendingByCategoryResponse(Long categoryId, String categoryName, String icon, String color, BigDecimal amount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.icon = icon;
        this.color = color;
        this.amount = amount;
    }
}
