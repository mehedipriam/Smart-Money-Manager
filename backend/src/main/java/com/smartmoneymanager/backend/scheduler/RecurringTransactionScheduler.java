package com.smartmoneymanager.backend.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.entity.RecurringTransaction;
import com.smartmoneymanager.backend.repository.RecurringTransactionRepository;
import com.smartmoneymanager.backend.service.RecurringTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sweeps every active recurring-transaction template due to fire and asks
 * {@link RecurringTransactionService} to process each one in its own
 * transaction, so one template's failure can't block the rest of the batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "${app.scheduler.recurring-transactions-cron:0 5 0 * * *}")
    public void processDueRecurringTransactions() {
        List<Long> dueIds = recurringTransactionRepository
                .findAllByActiveTrueAndNextRunDateLessThanEqual(LocalDate.now())
                .stream()
                .map(RecurringTransaction::getId)
                .toList();

        if (dueIds.isEmpty()) {
            return;
        }
        log.info("Processing {} due recurring transaction(s)", dueIds.size());

        for (Long id : dueIds) {
            try {
                recurringTransactionService.processDue(id);
            } catch (Exception e) {
                log.error("Failed to process recurring transaction {}", id, e);
            }
        }
    }
}
