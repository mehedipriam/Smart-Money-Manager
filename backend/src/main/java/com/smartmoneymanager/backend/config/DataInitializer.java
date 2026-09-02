package com.smartmoneymanager.backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.smartmoneymanager.backend.entity.Category;
import static com.smartmoneymanager.backend.entity.Category.TRANSFER_CATEGORY_NAME;
import com.smartmoneymanager.backend.entity.Role;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.RoleName;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds fixed lookup / default data on startup, if not already present:
 * the {@code ROLE_USER} / {@code ROLE_ADMIN} rows, and the default (global,
 * user-less) income and expense categories every user starts with.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private record DefaultCategory(String name, CategoryType type, String icon, String color) {
    }

    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("Food & Dining", CategoryType.EXPENSE, "🍔", "#f97316"),
            new DefaultCategory("Shopping", CategoryType.EXPENSE, "🛍️", "#ec4899"),
            new DefaultCategory("Transport", CategoryType.EXPENSE, "🚗", "#3b82f6"),
            new DefaultCategory("Education", CategoryType.EXPENSE, "🎓", "#6366f1"),
            new DefaultCategory("Entertainment", CategoryType.EXPENSE, "🎬", "#a855f7"),
            new DefaultCategory("Health", CategoryType.EXPENSE, "🏥", "#ef4444"),
            new DefaultCategory("Bills & Utilities", CategoryType.EXPENSE, "💡", "#eab308"),
            new DefaultCategory("Rent", CategoryType.EXPENSE, "🏠", "#14b8a6"),
            new DefaultCategory("Travel", CategoryType.EXPENSE, "✈️", "#06b6d4"),
            new DefaultCategory("Others", CategoryType.EXPENSE, "📦", "#6b7280"),
            new DefaultCategory("Salary", CategoryType.INCOME, "💼", "#16a34a"),
            new DefaultCategory("Freelance", CategoryType.INCOME, "💻", "#22c55e"),
            new DefaultCategory("Business", CategoryType.INCOME, "🏢", "#0ea5e9"),
            new DefaultCategory("Investment", CategoryType.INCOME, "📈", "#10b981"),
            new DefaultCategory("Gift", CategoryType.INCOME, "🎁", "#f43f5e"),
            new DefaultCategory("Others", CategoryType.INCOME, "📦", "#6b7280"),
            // System category used to tag the two transaction rows an account-to-account
            // transfer writes (see AccountServiceImpl.transfer / TRANSFER_CATEGORY_NAME).
            new DefaultCategory(TRANSFER_CATEGORY_NAME, CategoryType.EXPENSE, "🔁", "#64748b"),
            new DefaultCategory(TRANSFER_CATEGORY_NAME, CategoryType.INCOME, "🔁", "#64748b"));

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                log.info("Seeding missing role: {}", roleName);
                return roleRepository.save(Role.builder().name(roleName).build());
            });
        }

        for (DefaultCategory defaultCategory : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameAndTypeAndUserIsNull(defaultCategory.name(), defaultCategory.type())) {
                log.info("Seeding missing default category: {} ({})", defaultCategory.name(), defaultCategory.type());
                categoryRepository.save(Category.builder()
                        .user(null)
                        .name(defaultCategory.name())
                        .type(defaultCategory.type())
                        .icon(defaultCategory.icon())
                        .color(defaultCategory.color())
                        .isDefault(true)
                        .build());
            }
        }
    }
}
