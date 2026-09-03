package com.smartmoneymanager.backend.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmoneymanager.backend.entity.Bill;
import com.smartmoneymanager.backend.entity.enums.BillPaymentStatus;
import com.smartmoneymanager.backend.entity.enums.NotificationType;
import com.smartmoneymanager.backend.repository.BillRepository;
import com.smartmoneymanager.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Notifies each bill's owner exactly twice per bill, on fixed day-offsets
 * from its due date ({@code REMINDER_DAYS_AHEAD} days out, and again on the
 * due date itself) rather than "due within N days" every run — since the
 * due date doesn't change, matching an exact offset naturally fires once
 * per bill per day without needing a separate "already reminded" flag.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillReminderScheduler {

    private static final int REMINDER_DAYS_AHEAD = 3;

    private final BillRepository billRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.scheduler.bill-reminders-cron:0 10 0 * * *}")
    public void sendDueReminders() {
        LocalDate today = LocalDate.now();
        remind(today.plusDays(REMINDER_DAYS_AHEAD), "is due in " + REMINDER_DAYS_AHEAD + " days");
        remind(today, "is due today");
    }

    private void remind(LocalDate dueDate, String phrase) {
        List<Bill> bills = billRepository.findAllByPaymentStatusAndDueDate(BillPaymentStatus.PENDING, dueDate);
        if (bills.isEmpty()) {
            return;
        }
        log.info("Sending {} bill due reminder(s) for {}", bills.size(), dueDate);
        for (Bill bill : bills) {
            notificationService.notify(bill.getUser().getId(), NotificationType.BILL_DUE_REMINDER, "Bill due reminder",
                    bill.getBillName() + " (" + bill.getAmount() + ") " + phrase + ".");
        }
    }
}
