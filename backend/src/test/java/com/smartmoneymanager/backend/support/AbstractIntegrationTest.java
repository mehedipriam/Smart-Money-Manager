package com.smartmoneymanager.backend.support;

import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.smartmoneymanager.backend.entity.Role;
import com.smartmoneymanager.backend.entity.User;
import com.smartmoneymanager.backend.entity.enums.RoleName;
import com.smartmoneymanager.backend.repository.EmailVerificationTokenRepository;
import com.smartmoneymanager.backend.repository.RoleRepository;
import com.smartmoneymanager.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared MockMvc + test-data plumbing for the integration test suite. Every
 * subclass gets its own H2 database (see application-test.properties, whose
 * datasource URL is randomized per Spring context) and every helper here
 * drives real HTTP endpoints — register/verify/login — rather than poking
 * entities directly, so the tests exercise the exact same path a real client
 * would.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected EmailVerificationTokenRepository emailVerificationTokenRepository;

    /** Registers via the real endpoint, then verifies the email using the token the flow actually issued (no shortcuts). */
    protected Long registerVerifiedUser(String fullName, String email, String password) throws Exception {
        String registerBody = """
                {"fullName":"%s","email":"%s","password":"%s"}
                """.formatted(fullName, email, password);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail(email).orElseThrow();
        String token = emailVerificationTokenRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                .orElseThrow().getToken();

        mockMvc.perform(get("/api/auth/verify-email").param("token", token))
                .andExpect(status().isOk());

        return user.getId();
    }

    protected String login(String email, String password) throws Exception {
        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    /** Registers, verifies, logs in, and returns the resulting access token — the common case for a plain user. */
    protected String registerLoginAndGetToken(String fullName, String email, String password) throws Exception {
        registerVerifiedUser(fullName, email, password);
        return login(email, password);
    }

    /** Grants ROLE_ADMIN to an already-registered user, replacing ROLE_USER (mirrors the seeded admin's role set). */
    protected void promoteToAdmin(Long userId) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN is not seeded"));
        User user = userRepository.findById(userId).orElseThrow();
        user.setRoles(Set.of(adminRole));
        userRepository.save(user);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
