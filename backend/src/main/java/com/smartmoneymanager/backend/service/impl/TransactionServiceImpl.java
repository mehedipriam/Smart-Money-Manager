package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.CreateTransactionRequest;
import com.smartmoneymanager.backend.dto.request.TransactionFilter;
import com.smartmoneymanager.backend.dto.request.UpdateTransactionRequest;
import com.smartmoneymanager.backend.dto.response.TransactionResponse;
import com.smartmoneymanager.backend.entity.Account;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.entity.enums.NotificationType;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.TransactionMapper;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.BudgetRepository;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.NotificationService;
import com.smartmoneymanager.backend.service.TransactionService;
import com.smartmoneymanager.backend.specification.TransactionSpecifications;

import lombok.RequiredArgsConstructor;

/**
 * Every transaction mutation here also keeps {@code Account.currentBalance}
 * in sync (INCOME adds, EXPENSE subtracts), the same running-total model
 * established for accounts in Phase 4. Regular transactions never block on
 * insufficient balance — unlike a transfer, an expense you record is simply
 * a fact you're logging, not money the system is required to reserve first.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    /** Usage percentage at or above which a BUDGET_WARNING notification fires (below BUDGET_EXCEEDED's 100%). */
    private static final BigDecimal BUDGET_WARNING_THRESHOLD = BigDecimal.valueOf(80);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(Long userId, TransactionFilter filter, Pageable pageable) {
        if (filter.dateFrom() != null && filter.dateTo() != null && filter.dateFrom().isAfter(filter.dateTo())) {
            throw new InvalidOperationException("dateFrom cannot be after dateTo");
        }
        if (filter.amountFrom() != null && filter.amountTo() != null && filter.amountFrom().compareTo(filter.amountTo()) > 0) {
            throw new InvalidOperationException("amountFrom cannot be greater than amountTo");
        }

        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.belongsToUser(userId))
                .and(TransactionSpecifications.hasAccount(filter.accountId()))
                .and(TransactionSpecifications.hasCategory(filter.categoryId()))
                .and(TransactionSpecifications.hasType(filter.type()))
                .and(TransactionSpecifications.dateFrom(filter.dateFrom()))
                .and(TransactionSpecifications.dateTo(filter.dateTo()))
                .and(TransactionSpecifications.amountFrom(filter.amountFrom()))
                .and(TransactionSpecifications.amountTo(filter.amountTo()))
                .and(TransactionSpecifications.descriptionOrNoteContains(filter.search()));

        Page<TransactionResponse> page = transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long userId, Long transactionId) {
        return transactionMapper.toResponse(findOwnedTransaction(userId, transactionId));
    }

    @Override
    public TransactionResponse createTransaction(Long userId, CreateTransactionRequest request) {
        User user = userRepository.getReferenceById(userId);
        Account account = findOwnedAccount(userId, request.getAccountId());
        Category category = findVisibleCategory(userId, request.getCategoryId(), request.getType());

        Transaction transaction = createSystemTransaction(
                user, account, category, request.getType(), request.getAmount(),
                request.getTransactionDate(), request.getDescription(), request.getNote());

        if (request.getType() == TransactionType.EXPENSE) {
            checkBudgetThresholds(userId, category, request.getTransactionDate(), request.getAmount());
        }

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(Long userId, Long transactionId, UpdateTransactionRequest request) {
        Transaction transaction = findOwnedTransaction(userId, transactionId);

        // Undo the old transaction's effect on its (possibly about-to-change) account.
        reverseEffect(transaction.getAccount(), transaction.getType(), transaction.getAmount());

        Account newAccount = findOwnedAccount(userId, request.getAccountId());
        Category newCategory = findVisibleCategory(userId, request.getCategoryId(), request.getType());

        applyEffect(newAccount, request.getType(), request.getAmount());
        accountRepository.save(newAccount);
        if (!newAccount.getId().equals(transaction.getAccount().getId())) {
            accountRepository.save(transaction.getAccount());
        }

        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setNote(request.getNote());

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = findOwnedTransaction(userId, transactionId);
        reverseEffect(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        accountRepository.save(transaction.getAccount());
        transactionRepository.delete(transaction);
    }

    @Override
    public Transaction createSystemTransaction(
            User user, Account account, Category category, TransactionType type,
            BigDecimal amount, LocalDate date, String description, String note) {
        applyEffect(account, type, amount);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .type(type)
                .amount(amount)
                .transactionDate(date)
                .description(description)
                .note(note)
                .build();
        return transactionRepository.save(transaction);
    }

    /**
     * Fires a BUDGET_WARNING/BUDGET_EXCEEDED notification exactly on the
     * transaction that pushes usage across the 80% or 100% threshold for
     * that category's budget in {@code date}'s month — never again on
     * subsequent transactions once already past it. Scoped to transaction
     * creation only; editing an existing transaction doesn't re-check.
     */
    private void checkBudgetThresholds(Long userId, Category category, LocalDate date, BigDecimal amount) {
        YearMonth yearMonth = YearMonth.from(date);
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, category.getId(), yearMonth.getMonthValue(), yearMonth.getYear())
                .ifPresent(budget -> {
                    if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return;
                    }
                    BigDecimal usedAfter = transactionRepository.sumAmountByCategory(
                            userId, category.getId(), TransactionType.EXPENSE, yearMonth.atDay(1), yearMonth.atEndOfMonth());
                    BigDecimal usedBefore = usedAfter.subtract(amount);
                    BigDecimal percentBefore = usagePercentage(usedBefore, budget.getBudgetAmount());
                    BigDecimal percentAfter = usagePercentage(usedAfter, budget.getBudgetAmount());

                    if (percentAfter.compareTo(BigDecimal.valueOf(100)) >= 0 && percentBefore.compareTo(BigDecimal.valueOf(100)) < 0) {
                        notificationService.notify(userId, NotificationType.BUDGET_EXCEEDED, "Budget exceeded",
                                "You've exceeded your " + category.getName() + " budget for " + yearMonth + ".");
                    } else if (percentAfter.compareTo(BUDGET_WARNING_THRESHOLD) >= 0 && percentBefore.compareTo(BUDGET_WARNING_THRESHOLD) < 0) {
                        notificationService.notify(userId, NotificationType.BUDGET_WARNING, "Budget warning",
                                "You've used " + percentAfter + "% of your " + category.getName() + " budget for " + yearMonth + ".");
                    }
                });
    }

    private BigDecimal usagePercentage(BigDecimal used, BigDecimal budgetAmount) {
        return used.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private void applyEffect(Account account, TransactionType type, BigDecimal amount) {
        account.setCurrentBalance(type == TransactionType.INCOME
                ? account.getCurrentBalance().add(amount)
                : account.getCurrentBalance().subtract(amount));
    }

    private void reverseEffect(Account account, TransactionType type, BigDecimal amount) {
        account.setCurrentBalance(type == TransactionType.INCOME
                ? account.getCurrentBalance().subtract(amount)
                : account.getCurrentBalance().add(amount));
    }

    private Account findOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Category findVisibleCategory(Long userId, Long categoryId, TransactionType transactionType) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean visible = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!visible) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (!category.getType().name().equals(transactionType.name())) {
            throw new InvalidOperationException("Category type does not match transaction type");
        }
        return category;
    }

    private Transaction findOwnedTransaction(Long userId, Long transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }
}
