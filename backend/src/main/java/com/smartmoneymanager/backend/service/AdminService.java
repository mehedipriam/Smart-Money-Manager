package com.smartmoneymanager.backend.service;

import org.springframework.data.domain.Pageable;

import com.smartmoneymanager.backend.dto.common.PageResponse;
import com.smartmoneymanager.backend.dto.request.AdminUserFilter;
import com.smartmoneymanager.backend.dto.response.AdminStatsResponse;
import com.smartmoneymanager.backend.dto.response.AdminUserResponse;

public interface AdminService {

    AdminStatsResponse getStats();

    PageResponse<AdminUserResponse> getUsers(AdminUserFilter filter, Pageable pageable);

    AdminUserResponse getUser(Long userId);

    AdminUserResponse setUserEnabled(Long adminId, Long userId, boolean enabled);
}
