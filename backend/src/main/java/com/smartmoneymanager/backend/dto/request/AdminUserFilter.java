package com.smartmoneymanager.backend.dto.request;

/** Plain carrier for the optional query-string filters on GET /api/admin/users — built by the controller, not bound/validated from a JSON body. */
public record AdminUserFilter(String search, Boolean enabled) {
}
