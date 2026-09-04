package com.smartmoneymanager.backend.service.impl;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage for the two business rules in AdminServiceImpl that
 * aren't purely "call the repository": the self-disable guard, and 404 on a
 * missing user. The broader "admin-only, IDOR-safe" behavior is covered at
 * the HTTP layer by AdminAuthorizationSecurityTest.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void adminCannotDisableOwnAccount() {
        assertThatThrownBy(() -> adminService.setUserEnabled(42L, 42L, false))
                .isInstanceOf(InvalidOperationException.class);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminCanDisableAnotherUser() {
        User target = User.builder().fullName("Target").email("target@example.com").enabled(true).build();
        when(userRepository.findById(7L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);
        when(userMapper.toAdminUserResponse(target)).thenReturn(
                AdminUserResponse.builder().id(7L).fullName("Target").enabled(false).build());

        AdminUserResponse response = adminService.setUserEnabled(1L, 7L, false);

        assertThat(response.isEnabled()).isFalse();
        assertThat(target.isEnabled()).isFalse();
    }

    @Test
    void disablingAMissingUserThrowsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.setUserEnabled(1L, 99L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
