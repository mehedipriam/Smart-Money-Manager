package com.smartmoneymanager.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.support.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the authentication boundary itself: registration validation,
 * unverified/disabled login rejection, protected-route access with and
 * without a token, and rejection of a tampered/garbage JWT. These are the
 * checks from spec section 18 ("Prevent unauthorized data access / invalid
 * JWT access") that apply before any ownership check ever runs.
 */
class AuthenticationSecurityTest extends AbstractIntegrationTest {

    @Test
    void registerRejectsWeakPassword() throws Exception {
        String body = """
                {"fullName":"Weak Pw","email":"weak-pw@example.com","password":"short"}
                """;

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        registerVerifiedUser("First User", "dupe@example.com", "Password1");

        String body = """
                {"fullName":"Second User","email":"dupe@example.com","password":"Password1"}
                """;

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginBeforeEmailVerificationIsRejected() throws Exception {
        String registerBody = """
                {"fullName":"Not Verified","email":"unverified@example.com","password":"Password1"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isOk());

        String loginBody = """
                {"email":"unverified@example.com","password":"Password1"}
                """;
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        registerVerifiedUser("Right Pw", "wrongpw@example.com", "Password1");

        String loginBody = """
                {"email":"wrongpw@example.com","password":"NotThePassword1"}
                """;
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void protectedEndpointWithGarbageTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer("not-a-real-jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithValidTokenSucceeds() throws Exception {
        String token = registerLoginAndGetToken("Valid Token", "validtoken@example.com", "Password1");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("validtoken@example.com"));
    }

    @Test
    void disabledAccountLosesAccessOnNextRequestEvenWithAStillValidToken() throws Exception {
        Long userId = registerVerifiedUser("To Disable", "todisable@example.com", "Password1");
        String token = login("todisable@example.com", "Password1");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        User disabledUser = userRepository.findById(userId).orElseThrow();
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        // Same, still-unexpired token — JwtAuthenticationFilter re-checks isEnabled() from the DB on every request.
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }
}
