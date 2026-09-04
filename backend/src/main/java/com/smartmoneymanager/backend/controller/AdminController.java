package com.smartmoneymanager.backend.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.AdminUserFilter;
import com.smartmoneymanager.backend.dto.response.AdminStatsResponse;
import com.smartmoneymanager.backend.dto.response.AdminUserResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.AdminService;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only endpoints (Phase 13). Gated by SecurityConfig's
 * {@code /api/admin/** -> hasRole("ADMIN")} rule, so every method here is
 * already unreachable by a plain ROLE_USER account.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "fullName", "email");

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("System statistics fetched", adminService.getStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = SORTABLE_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, safeSortBy);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        AdminUserFilter filter = new AdminUserFilter(search, enabled);
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getUsers(filter, pageable)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched", adminService.getUser(id)));
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<AdminUserResponse>> enableUser(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User enabled", adminService.setUserEnabled(principal.getId(), id, true)));
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<ApiResponse<AdminUserResponse>> disableUser(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User disabled", adminService.setUserEnabled(principal.getId(), id, false)));
    }
}
