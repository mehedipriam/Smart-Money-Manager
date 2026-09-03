package com.smartmoneymanager.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmoneymanager.backend.dto.projection.TransactionAmountProjection;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.enums.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumAmount(@Param("userId") Long userId, @Param("type") TransactionType type,
            @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.type = :type "
            + "AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumAmountByCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId,
            @Param("type") TransactionType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT new com.smartmoneymanager.backend.dto.projection.TransactionAmountProjection(t.type, t.amount, t.transactionDate) "
            + "FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :start AND :end")
    List<TransactionAmountProjection> findAmountsInRange(
            @Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT new com.smartmoneymanager.backend.dto.response.SpendingByCategoryResponse("
            + "c.id, c.name, c.icon, c.color, SUM(t.amount)) "
            + "FROM Transaction t JOIN t.category c "
            + "WHERE t.user.id = :userId AND t.type = :type "
            + "AND t.transactionDate BETWEEN :start AND :end "
            + "GROUP BY c.id, c.name, c.icon, c.color "
            + "ORDER BY SUM(t.amount) DESC")
    List<com.smartmoneymanager.backend.dto.response.SpendingByCategoryResponse> findSpendingByCategory(
            @Param("userId") Long userId, @Param("type") TransactionType type,
            @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.category JOIN FETCH t.account "
            + "WHERE t.user.id = :userId AND t.transactionDate BETWEEN :start AND :end "
            + "ORDER BY t.transactionDate ASC, t.id ASC")
    List<Transaction> findForExport(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
