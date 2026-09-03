package com.smartmoneymanager.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmoneymanager.backend.entity.Bill;
import com.smartmoneymanager.backend.entity.enums.BillPaymentStatus;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findAllByUserIdOrderByDueDateAsc(Long userId);

    List<Bill> findAllByUserIdAndPaymentStatusOrderByDueDateAsc(Long userId, BillPaymentStatus paymentStatus);

    Optional<Bill> findByIdAndUserId(Long id, Long userId);

    /** Every still-PENDING bill whose due date has passed — used to flip them to OVERDUE on read. */
    List<Bill> findAllByUserIdAndPaymentStatusAndDueDateBefore(Long userId, BillPaymentStatus paymentStatus, LocalDate date);

    List<Bill> findAllByUserIdAndPaymentStatusNotOrderByDueDateAsc(Long userId, BillPaymentStatus paymentStatus);

    /** Every user's still-unpaid bill due on an exact date — used by the reminder scheduler, across all users. */
    List<Bill> findAllByPaymentStatusAndDueDate(BillPaymentStatus paymentStatus, LocalDate dueDate);
}
