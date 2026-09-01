package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.CreateAccountRequest;
import com.smartmoneymanager.backend.dto.request.TransferRequest;
import com.smartmoneymanager.backend.dto.request.UpdateAccountRequest;
import com.smartmoneymanager.backend.dto.response.AccountResponse;
import com.smartmoneymanager.backend.dto.response.TransferResponse;
import com.smartmoneymanager.backend.entity.Account;
import com.smartmoneymanager.backend.exception.InsufficientBalanceException;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceInUseException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.AccountMapper;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long userId) {
        return accountRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long userId, Long accountId) {
        return accountMapper.toResponse(findOwnedAccount(userId, accountId));
    }

    @Override
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        Account account = Account.builder()
                .user(userRepository.getReferenceById(userId))
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .initialBalance(request.getInitialBalance())
                .currentBalance(request.getInitialBalance())
                .currency(request.getCurrency())
                .build();
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse updateAccount(Long userId, Long accountId, UpdateAccountRequest request) {
        Account account = findOwnedAccount(userId, accountId);
        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public void deleteAccount(Long userId, Long accountId) {
        Account account = findOwnedAccount(userId, accountId);
        try {
            accountRepository.delete(account);
            accountRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete an account that has existing transactions");
        }
    }

    @Override
    public TransferResponse transfer(Long userId, TransferRequest request) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new InvalidOperationException("Cannot transfer money to the same account");
        }

        Account fromAccount = findOwnedAccount(userId, request.getFromAccountId());
        Account toAccount = findOwnedAccount(userId, request.getToAccountId());

        BigDecimal amount = request.getAmount();
        if (fromAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in the source account");
        }

        fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().subtract(amount));
        toAccount.setCurrentBalance(toAccount.getCurrentBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // NOTE: this only moves the balances. Once Category Management (Phase 5) and
        // Transaction Management (Phase 6) exist, this should also write the two
        // linked transaction rows (EXPENSE on fromAccount, INCOME on toAccount,
        // tagged with a "Transfer" category) described in docs/PHASE_2_DATABASE_SCHEMA.md,
        // so transfers show up in transaction history and reports.
        return TransferResponse.builder()
                .fromAccount(accountMapper.toResponse(fromAccount))
                .toAccount(accountMapper.toResponse(toAccount))
                .build();
    }

    private Account findOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
