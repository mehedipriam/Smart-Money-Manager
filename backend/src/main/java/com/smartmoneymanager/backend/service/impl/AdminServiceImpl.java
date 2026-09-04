package com.smartmoneymanager.backend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.AdminUserFilter;
import com.smartmoneymanager.backend.dto.response.AdminStatsResponse;
import com.smartmoneymanager.backend.dto.response.AdminUserResponse;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.UserMapper;
import com.smartmoneymanager.backend.repository.AccountRepository;
import com.smartmoneymanager.backend.repository.BillRepository;
import com.smartmoneymanager.backend.repository.BudgetRepository;
import com.smartmoneymanager.backend.repository.GoalRepository;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.AdminService;
import com.smartmoneymanager.backend.specification.UserSpecifications;

import lombok.RequiredArgsConstructor;

/**
 * Backs the admin panel (Phase 13). Reads here are deliberately limited to
 * user profile/account-status fields and system-wide counters — never a
 * user's individual accounts, transactions, budgets, goals or bills — per
 * the spec's requirement that admins not view others' financial details.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;
    private final BillRepository billRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByEnabledTrue())
                .disabledUsers(userRepository.countByEnabledFalse())
                .verifiedUsers(userRepository.countByEmailVerifiedTrue())
                .newUsersThisMonth(userRepository.countByCreatedAtGreaterThanEqual(startOfMonth))
                .totalTransactions(transactionRepository.count())
                .totalAccounts(accountRepository.count())
                .totalBudgets(budgetRepository.count())
                .totalGoals(goalRepository.count())
                .totalBills(billRepository.count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(AdminUserFilter filter, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecifications.fullNameOrEmailContains(filter.search()))
                .and(UserSpecifications.hasEnabled(filter.enabled()));

        Page<AdminUserResponse> page = userRepository.findAll(spec, pageable).map(userMapper::toAdminUserResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        return userMapper.toAdminUserResponse(findUser(userId));
    }

    @Override
    public AdminUserResponse setUserEnabled(Long adminId, Long userId, boolean enabled) {
        if (!enabled && adminId.equals(userId)) {
            throw new InvalidOperationException("You cannot disable your own account");
        }

        User user = findUser(userId);
        user.setEnabled(enabled);
        return userMapper.toAdminUserResponse(userRepository.save(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
