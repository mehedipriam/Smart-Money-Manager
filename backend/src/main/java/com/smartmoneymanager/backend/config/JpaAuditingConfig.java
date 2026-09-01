package com.smartmoneymanager.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables automatic population of @CreatedDate / @LastModifiedDate fields
 * declared on {@link com.smartmoneymanager.backend.entity.AuditableEntity}
 * and {@link com.smartmoneymanager.backend.entity.CreatedOnlyEntity}.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
