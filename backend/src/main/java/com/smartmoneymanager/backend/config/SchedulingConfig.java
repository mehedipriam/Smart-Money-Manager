package com.smartmoneymanager.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} beans, e.g. {@code RecurringTransactionScheduler}. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
