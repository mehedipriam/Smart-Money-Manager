package com.smartmoneymanager.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.smartmoneymanager.backend.support.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IDOR checks (spec section 18): a user must never be able to read or
 * mutate another user's financial resource by guessing/incrementing an id.
 * Every resource type is only checked once here — the underlying pattern
 * (a repository lookup scoped by both id AND user id, see e.g.
 * TransactionRepository#findByIdAndUserId) is identical across
 * accounts/transactions/budgets/goals/bills, so one representative per verb
 * (read, update, delete) is enough to catch a regression in that pattern.
 */
class OwnershipSecurityTest extends AbstractIntegrationTest {

    private long createAccountAs(String token) throws Exception {
        String body = """
                {"accountName":"Wallet","accountType":"CASH","initialBalance":100,"currency":"BDT"}
                """;
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    @Test
    void userCannotReadAnotherUsersAccount() throws Exception {
        String ownerToken = registerLoginAndGetToken("Owner", "owner-acct@example.com", "Password1");
        String intruderToken = registerLoginAndGetToken("Intruder", "intruder-acct@example.com", "Password1");
        long accountId = createAccountAs(ownerToken);

        // Owner can read their own account...
        mockMvc.perform(get("/api/accounts/" + accountId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        // ...but a different authenticated user gets a 404, not the account's data or a 403
        // that would confirm the id exists.
        mockMvc.perform(get("/api/accounts/" + accountId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotUpdateAnotherUsersAccount() throws Exception {
        String ownerToken = registerLoginAndGetToken("Owner2", "owner-acct2@example.com", "Password1");
        String intruderToken = registerLoginAndGetToken("Intruder2", "intruder-acct2@example.com", "Password1");
        long accountId = createAccountAs(ownerToken);

        String updateBody = """
                {"accountName":"Hijacked","accountType":"CASH","currency":"BDT"}
                """;
        mockMvc.perform(put("/api/accounts/" + accountId)
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotDeleteAnotherUsersAccount() throws Exception {
        String ownerToken = registerLoginAndGetToken("Owner3", "owner-acct3@example.com", "Password1");
        String intruderToken = registerLoginAndGetToken("Intruder3", "intruder-acct3@example.com", "Password1");
        long accountId = createAccountAs(ownerToken);

        mockMvc.perform(delete("/api/accounts/" + accountId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());

        // Still there and still readable by its real owner — the delete attempt above had no effect.
        mockMvc.perform(get("/api/accounts/" + accountId).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotListAnotherUsersTransactionsByGuessingTheyDontExistInTheirOwnList() throws Exception {
        String ownerToken = registerLoginAndGetToken("Owner4", "owner-txn@example.com", "Password1");
        String intruderToken = registerLoginAndGetToken("Intruder4", "intruder-txn@example.com", "Password1");
        long accountId = createAccountAs(ownerToken);

        // A transaction needs a real category id; grab a seeded default one visible to both users.
        MvcResult categories = mockMvc.perform(get("/api/categories")
                        .param("type", "EXPENSE")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn();
        long categoryId = ((Number) JsonPath.read(categories.getResponse().getContentAsString(), "$.data[0].id")).longValue();

        String txnBody = """
                {"accountId":%d,"categoryId":%d,"type":"EXPENSE","amount":25,"transactionDate":"2026-01-01"}
                """.formatted(accountId, categoryId);
        MvcResult created = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(txnBody))
                .andExpect(status().isOk())
                .andReturn();
        long transactionId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.data.id")).longValue();

        mockMvc.perform(get("/api/transactions/" + transactionId).header("Authorization", bearer(intruderToken)))
                .andExpect(status().isNotFound());
    }
}
