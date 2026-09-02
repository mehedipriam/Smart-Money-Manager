package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InsufficientBalanceException;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceInUseException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.AccountMapper;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.AccountService;
import com.smartmoneymanager.backend.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AccountMapper accountMapper;
    private final TransactionService transactionService;

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

        User user = userRepository.getReferenceById(userId);
        LocalDate today = LocalDate.now();
        Category transferExpenseCategory = findTransferCategory(CategoryType.EXPENSE);
        Category transferIncomeCategory = findTransferCategory(CategoryType.INCOME);

        // A transfer is recorded as two linked transactions — an EXPENSE leaving
        // fromAccount and an INCOME arriving in toAccount, both tagged with the
        // system "Transfer" category — rather than a separate ledger concept, so
        // transfers show up in transaction history/reports/budgets like anything
        // else. Each leg's own balance effect is applied by createSystemTransaction,
        // so the two calls together move the money exactly as a direct balance
        // edit would have.
        transactionService.createSystemTransaction(
                user, fromAccount, transferExpenseCategory, TransactionType.EXPENSE, amount, today,
                "Transfer to " + toAccount.getAccountName(), request.getNote());
        transactionService.createSystemTransaction(
                user, toAccount, transferIncomeCategory, TransactionType.INCOME, amount, today,
                "Transfer from " + fromAccount.getAccountName(), request.getNote());

        return TransferResponse.builder()
                .fromAccount(accountMapper.toResponse(fromAccount))
                .toAccount(accountMapper.toResponse(toAccount))
                .build();
    }

    private Category findTransferCategory(CategoryType type) {
        return categoryRepository.findByNameAndTypeAndUserIsNull(Category.TRANSFER_CATEGORY_NAME, type)
                .orElseThrow(() -> new IllegalStateException(
                        "System 'Transfer' category (" + type + ") is not seeded"));
    }

    private Account findOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
