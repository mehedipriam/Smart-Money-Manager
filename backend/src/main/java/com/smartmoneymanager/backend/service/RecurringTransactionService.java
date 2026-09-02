package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.CreateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.request.UpdateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.response.RecurringTransactionResponse;

public interface RecurringTransactionService {

    List<RecurringTransactionResponse> getRecurringTransactions(Long userId);

    RecurringTransactionResponse createRecurringTransaction(Long userId, CreateRecurringTransactionRequest request);

    RecurringTransactionResponse updateRecurringTransaction(Long userId, Long id, UpdateRecurringTransactionRequest request);

    void deleteRecurringTransaction(Long userId, Long id);

    /**
     * Generates every transaction this template owes (catching up on any
     * missed occurrences) and advances {@code nextRunDate} past today —
     * called by {@code RecurringTransactionScheduler}, one call per due
     * template so a failure in one never aborts the rest of the sweep.
     */
    void processDue(Long recurringTransactionId);
}
