package com.smartmoneymanager.backend.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.CreateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.request.UpdateRecurringTransactionRequest;
import com.smartmoneymanager.backend.dto.response.RecurringTransactionResponse;
import com.smartmoneymanager.backend.entity.Account;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.RecurringTransaction;
import com.smartmoneymanager.backend.entity.enums.NotificationType;
import com.smartmoneymanager.backend.entity.enums.RecurringFrequency;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.RecurringTransactionMapper;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.RecurringTransactionRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.NotificationService;
import com.smartmoneymanager.backend.service.RecurringTransactionService;
import com.smartmoneymanager.backend.service.TransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    /** Safety cap so one long-dormant template can't generate an unbounded number of catch-up transactions in a single sweep. */
    private static final int MAX_CATCHUP_OCCURRENCES = 366;

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    @Override
    public RecurringTransactionResponse createRecurringTransaction(Long userId, CreateRecurringTransactionRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidOperationException("End date cannot be before start date");
        }

        Account account = findOwnedAccount(userId, request.getAccountId());
        Category category = findVisibleCategory(userId, request.getCategoryId(), request.getType());

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(userRepository.getReferenceById(userId))
                .account(account)
                .category(category)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .note(request.getNote())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .nextRunDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Override
    public RecurringTransactionResponse updateRecurringTransaction(Long userId, Long id, UpdateRecurringTransactionRequest request) {
        RecurringTransaction recurring = findOwnedRecurring(userId, id);

        if (request.getEndDate() != null && request.getEndDate().isBefore(recurring.getStartDate())) {
            throw new InvalidOperationException("End date cannot be before start date");
        }

        Account account = findOwnedAccount(userId, request.getAccountId());
        Category category = findVisibleCategory(userId, request.getCategoryId(), request.getType());

        recurring.setAccount(account);
        recurring.setCategory(category);
        recurring.setType(request.getType());
        recurring.setAmount(request.getAmount());
        recurring.setDescription(request.getDescription());
        recurring.setNote(request.getNote());
        recurring.setFrequency(request.getFrequency());
        recurring.setEndDate(request.getEndDate());
        recurring.setActive(request.getActive());

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Override
    public void deleteRecurringTransaction(Long userId, Long id) {
        recurringTransactionRepository.delete(findOwnedRecurring(userId, id));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDue(Long recurringTransactionId) {
        RecurringTransaction recurring = recurringTransactionRepository.findById(recurringTransactionId).orElse(null);
        if (recurring == null || !recurring.isActive()) {
            return;
        }

        LocalDate today = LocalDate.now();
        int generated = 0;

        while (recurring.isActive() && !recurring.getNextRunDate().isAfter(today) && generated < MAX_CATCHUP_OCCURRENCES) {
            transactionService.createSystemTransaction(
                    recurring.getUser(), recurring.getAccount(), recurring.getCategory(), recurring.getType(),
                    recurring.getAmount(), recurring.getNextRunDate(), recurring.getDescription(), recurring.getNote());
            notificationService.notify(recurring.getUser().getId(), NotificationType.RECURRING_TRANSACTION_ADDED,
                    "Recurring transaction added",
                    (recurring.getDescription() != null ? recurring.getDescription() : recurring.getCategory().getName())
                            + " was automatically added for " + recurring.getNextRunDate() + ".");
            generated++;

            LocalDate next = advance(recurring.getNextRunDate(), recurring.getFrequency());
            if (recurring.getEndDate() != null && next.isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
            }
            recurring.setNextRunDate(next);
        }

        if (generated == MAX_CATCHUP_OCCURRENCES) {
            log.warn("Recurring transaction {} hit the catch-up cap ({} occurrences) in one sweep; "
                    + "remaining occurrences will generate on the next run.", recurringTransactionId, MAX_CATCHUP_OCCURRENCES);
        }

        recurringTransactionRepository.save(recurring);
    }

    private LocalDate advance(LocalDate date, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
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

    private RecurringTransaction findOwnedRecurring(Long userId, Long id) {
        return recurringTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found"));
    }
}
