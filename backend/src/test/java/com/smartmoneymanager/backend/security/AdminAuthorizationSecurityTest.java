package com.smartmoneymanager.backend.security;

import org.junit.jupiter.api.Test;

import com.smartmoneymanager.backend.support.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The admin panel (Phase 13) is only reachable with ROLE_ADMIN — enforced by
 * SecurityConfig's {@code /api/admin/** -> hasRole("ADMIN")} rule. These
 * tests pin that behavior down, plus AdminServiceImpl's self-disable guard
 * and the fact that admin responses never carry another user's financial
 * data (spec section 13: "admins must not directly view users' sensitive
 * financial transaction details").
 */
class AdminAuthorizationSecurityTest extends AbstractIntegrationTest {

    @Test
    void plainUserIsForbiddenFromAdminEndpoints() throws Exception {
        String token = registerLoginAndGetToken("Plain User", "plain-admin-check@example.com", "Password1");

        mockMvc.perform(get("/api/admin/stats").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestToAdminEndpointsIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanReadStatsAndUserListButNeverSeesFinancialFields() throws Exception {
        Long adminId = registerVerifiedUser("Site Admin", "site-admin@example.com", "Password1");
        promoteToAdmin(adminId);
        String adminToken = login("site-admin@example.com", "Password1");

        registerVerifiedUser("Regular Jane", "regular-jane@example.com", "Password1");

        mockMvc.perform(get("/api/admin/stats").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(2));

        mockMvc.perform(get("/api/admin/users").param("search", "Jane").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("regular-jane@example.com"))
                // AdminUserResponse must never expose a balance/amount-shaped field.
                .andExpect(jsonPath("$.data.content[0].accounts").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].transactions").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].balance").doesNotExist());
    }

    @Test
    void adminCannotDisableTheirOwnAccount() throws Exception {
        Long adminId = registerVerifiedUser("Self Admin", "self-admin@example.com", "Password1");
        promoteToAdmin(adminId);
        String adminToken = login("self-admin@example.com", "Password1");

        mockMvc.perform(put("/api/admin/users/" + adminId + "/disable").header("Authorization", bearer(adminToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disablingAUserBlocksTheirNextRequestEvenWithAnExistingToken() throws Exception {
        Long adminId = registerVerifiedUser("Admin Two", "admin-two@example.com", "Password1");
        promoteToAdmin(adminId);
        String adminToken = login("admin-two@example.com", "Password1");

        Long targetId = registerVerifiedUser("Target User", "target-user@example.com", "Password1");
        String targetToken = login("target-user@example.com", "Password1");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(targetToken)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/users/" + targetId + "/disable").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(targetToken)))
                .andExpect(status().isUnauthorized());
    }
}
