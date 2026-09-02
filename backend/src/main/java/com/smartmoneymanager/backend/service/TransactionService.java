package com.smartmoneymanager.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;

import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.CreateTransactionRequest;
import com.smartmoneymanager.backend.dto.request.TransactionFilter;
import com.smartmoneymanager.backend.dto.request.UpdateTransactionRequest;
import com.smartmoneymanager.backend.dto.response.TransactionResponse;
import com.smartmoneymanager.backend.entity.Account;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.entity.enums.TransactionType;

public interface TransactionService {

    PageResponse<TransactionResponse> getTransactions(Long userId, TransactionFilter filter, Pageable pageable);

    TransactionResponse getTransaction(Long userId, Long transactionId);

    TransactionResponse createTransaction(Long userId, CreateTransactionRequest request);

    TransactionResponse updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request);

    void deleteTransaction(Long userId, Long transactionId);

    /**
     * Creates a transaction and applies its balance effect, bypassing normal
     * request validation/ownership checks — for use by other services only
     * (account transfers, the recurring-transaction scheduler), never
     * exposed through a controller.
     */
    Transaction createSystemTransaction(
            User user, Account account, Category category, TransactionType type,
            BigDecimal amount, LocalDate date, String description, String note);
}
