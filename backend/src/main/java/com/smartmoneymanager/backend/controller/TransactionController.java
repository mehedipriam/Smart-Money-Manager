package com.smartmoneymanager.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.CreateTransactionRequest;
import com.smartmoneymanager.backend.dto.request.TransactionFilter;
import com.smartmoneymanager.backend.dto.request.UpdateTransactionRequest;
import com.smartmoneymanager.backend.dto.response.TransactionResponse;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("transactionDate", "amount", "createdAt", "description");

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "transactionDate";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, safeSortBy);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        TransactionFilter filter = new TransactionFilter(accountId, categoryId, type, dateFrom, dateTo, amountFrom, amountTo, search);
        PageResponse<TransactionResponse> result = transactionService.getTransactions(principal.getId(), filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", transactionService.getTransaction(principal.getId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse created = transactionService.createTransaction(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Transaction created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        TransactionResponse updated = transactionService.updateTransaction(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        transactionService.deleteTransaction(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted"));
    }
}
