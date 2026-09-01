package com.smartmoneymanager.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A monthly spending limit for one category. Usage (spent amount, remaining,
 * percentage) is derived at query time from {@link Transaction} rows rather
 * than stored, so it is always in sync.
 */
@Entity
@Table(name = "budgets",
        indexes = @Index(name = "idx_budgets_user_period", columnList = "user_id, year, month"),
        uniqueConstraints = @UniqueConstraint(
                name = "uq_budget_user_category_period",
                columnNames = { "user_id", "category_id", "month", "year" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "budget_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal budgetAmount;

    /** 1-12 */
    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;
}
