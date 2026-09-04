package com.smartmoneymanager.backend.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A user as seen by the admin panel: profile/account-status fields only.
 * Deliberately excludes financial data (accounts, transactions, budgets, ...)
 * per the spec's "admins must not directly view users' sensitive financial
 * transaction details" rule.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private boolean emailVerified;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
