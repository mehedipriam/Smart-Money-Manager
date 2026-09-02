package com.smartmoneymanager.backend.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.CreateBillRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBillRequest;
import com.smartmoneymanager.backend.dto.response.BillResponse;
import com.smartmoneymanager.backend.entity.Bill;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.enums.BillPaymentStatus;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.RecurringFrequency;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.BillMapper;
import com.smartmoneymanager.backend.repository.BillRepository;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.BillService;

import lombok.RequiredArgsConstructor;

/**
 * A bill's PENDING -> OVERDUE transition is computed (and persisted) on every
 * read rather than by a scheduled job — simpler, and there's no user-visible
 * difference since nothing needs to act on the transition the instant it
 * happens (unlike recurring transactions, which must actually generate a
 * transaction on schedule).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BillMapper billMapper;

    @Override
    public List<BillResponse> getBills(Long userId, BillPaymentStatus status) {
        sweepOverdue(userId);
        List<Bill> bills = status == null
                ? billRepository.findAllByUserIdOrderByDueDateAsc(userId)
                : billRepository.findAllByUserIdAndPaymentStatusOrderByDueDateAsc(userId, status);
        return bills.stream().map(billMapper::toResponse).toList();
    }

    @Override
    public List<BillResponse> getUpcomingBills(Long userId, int limit) {
        sweepOverdue(userId);
        return billRepository.findAllByUserIdAndPaymentStatusNotOrderByDueDateAsc(userId, BillPaymentStatus.PAID).stream()
                .limit(limit)
                .map(billMapper::toResponse)
                .toList();
    }

    @Override
    public BillResponse getBill(Long userId, Long billId) {
        sweepOverdue(userId);
        return billMapper.toResponse(findOwnedBill(userId, billId));
    }

    @Override
    public BillResponse createBill(Long userId, CreateBillRequest request) {
        Category category = resolveCategory(userId, request.getCategoryId());

        Bill bill = Bill.builder()
                .user(userRepository.getReferenceById(userId))
                .billName(request.getBillName())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .category(category)
                .recurringType(request.getRecurringType())
                .paymentStatus(initialStatus(request.getDueDate()))
                .build();
        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    public BillResponse updateBill(Long userId, Long billId, UpdateBillRequest request) {
        Bill bill = findOwnedBill(userId, billId);
        Category category = resolveCategory(userId, request.getCategoryId());

        bill.setBillName(request.getBillName());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
        bill.setCategory(category);
        bill.setRecurringType(request.getRecurringType());
        if (bill.getPaymentStatus() != BillPaymentStatus.PAID) {
            bill.setPaymentStatus(initialStatus(request.getDueDate()));
        }
        return billMapper.toResponse(billRepository.save(bill));
    }

    @Override
    public void deleteBill(Long userId, Long billId) {
        billRepository.delete(findOwnedBill(userId, billId));
    }

    @Override
    public BillResponse markAsPaid(Long userId, Long billId) {
        Bill bill = findOwnedBill(userId, billId);
        bill.setPaymentStatus(BillPaymentStatus.PAID);
        bill = billRepository.save(bill);

        if (bill.getRecurringType() != null) {
            Bill next = Bill.builder()
                    .user(bill.getUser())
                    .billName(bill.getBillName())
                    .amount(bill.getAmount())
                    .dueDate(advance(bill.getDueDate(), bill.getRecurringType()))
                    .category(bill.getCategory())
                    .recurringType(bill.getRecurringType())
                    .paymentStatus(BillPaymentStatus.PENDING)
                    .build();
            billRepository.save(next);
        }

        return billMapper.toResponse(bill);
    }

    /** Flips any PENDING bill whose due date has passed to OVERDUE, for this user, before any read. */
    private void sweepOverdue(Long userId) {
        List<Bill> overdue = billRepository.findAllByUserIdAndPaymentStatusAndDueDateBefore(
                userId, BillPaymentStatus.PENDING, LocalDate.now());
        if (overdue.isEmpty()) {
            return;
        }
        overdue.forEach(bill -> bill.setPaymentStatus(BillPaymentStatus.OVERDUE));
        billRepository.saveAll(overdue);
    }

    private BillPaymentStatus initialStatus(LocalDate dueDate) {
        return dueDate.isBefore(LocalDate.now()) ? BillPaymentStatus.OVERDUE : BillPaymentStatus.PENDING;
    }

    private LocalDate advance(LocalDate date, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }

    private Category resolveCategory(Long userId, Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean visible = category.getUser() == null || category.getUser().getId().equals(userId);
        if (!visible) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidOperationException("Bills can only be tagged with expense categories");
        }
        return category;
    }

    private Bill findOwnedBill(Long userId, Long billId) {
        return billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }
}
