package com.smartmoneymanager.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.request.CreateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.request.UpdateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.response.RecurringTransactionResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.RecurringTransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Recurring transactions fetched", recurringTransactionService.getRecurringTransactions(principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateRecurringTransactionRequest request) {
        RecurringTransactionResponse created = recurringTransactionService.createRecurringTransaction(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringTransactionRequest request) {
        RecurringTransactionResponse updated = recurringTransactionService.updateRecurringTransaction(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        recurringTransactionService.deleteRecurringTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted"));
    }
}
