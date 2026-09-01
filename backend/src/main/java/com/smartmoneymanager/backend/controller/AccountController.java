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
import com.smartmoneymanager.backend.dto.request.CreateAccountRequest;
import com.smartmoneymanager.backend.dto.request.TransferRequest;
import com.smartmoneymanager.backend.dto.request.UpdateAccountRequest;
import com.smartmoneymanager.backend.dto.response.AccountResponse;
import com.smartmoneymanager.backend.dto.response.TransferResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched", accountService.getAccounts(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Account fetched", accountService.getAccount(principal.getId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse created = accountService.createAccount(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Account created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse updated = accountService.updateAccount(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Account updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        accountService.deleteAccount(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody TransferRequest request) {
        TransferResponse result = accountService.transfer(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed", result));
    }
}
