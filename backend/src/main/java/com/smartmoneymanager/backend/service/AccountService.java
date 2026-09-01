package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.CreateAccountRequest;
import com.smartmoneymanager.backend.dto.request.TransferRequest;
import com.smartmoneymanager.backend.dto.request.UpdateAccountRequest;
import com.smartmoneymanager.backend.dto.response.AccountResponse;
import com.smartmoneymanager.backend.dto.response.TransferResponse;

public interface AccountService {

    List<AccountResponse> getAccounts(Long userId);

    AccountResponse getAccount(Long userId, Long accountId);

    AccountResponse createAccount(Long userId, CreateAccountRequest request);

    AccountResponse updateAccount(Long userId, Long accountId, UpdateAccountRequest request);

    void deleteAccount(Long userId, Long accountId);

    TransferResponse transfer(Long userId, TransferRequest request);
}
