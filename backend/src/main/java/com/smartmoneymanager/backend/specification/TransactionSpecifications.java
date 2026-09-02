package com.smartmoneymanager.backend.specification;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.enums.TransactionType;

/**
 * Builds a dynamic {@link Specification} for the transaction list/search/filter
 * endpoint. Every method always returns a non-null Specification — an
 * always-true (conjunction) predicate when its filter value is absent — so
 * callers can unconditionally chain them with {@code .and(...)}; this Spring
 * Data version's {@code Specification.and} throws on a null argument rather
 * than treating it as a no-op.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> hasAccount(Long accountId) {
        return (root, query, cb) -> accountId == null ? cb.conjunction() : cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> hasCategory(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> dateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("transactionDate"), from);
    }

    public static Specification<Transaction> dateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("transactionDate"), to);
    }

    public static Specification<Transaction> amountFrom(BigDecimal min) {
        return (root, query, cb) -> min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Transaction> amountTo(BigDecimal max) {
        return (root, query, cb) -> max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Transaction> descriptionOrNoteContains(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("note"), "")), pattern));
    }
}
