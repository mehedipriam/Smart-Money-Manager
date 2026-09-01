package com.smartmoneymanager.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.smartmoneymanager.backend.entity.Role;
import com.smartmoneymanager.backend.entity.enums.RoleName;
import com.smartmoneymanager.backend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Seeds the fixed {@code ROLE_USER} / {@code ROLE_ADMIN} lookup rows on startup, if not already present. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                log.info("Seeding missing role: {}", roleName);
                return roleRepository.save(Role.builder().name(roleName).build());
            });
        }
    }
}
