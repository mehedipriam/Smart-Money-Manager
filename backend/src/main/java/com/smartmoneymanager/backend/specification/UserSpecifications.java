package com.smartmoneymanager.backend.specification;

import org.springframework.data.jpa.domain.Specification;

import com.smartmoneymanager.backend.entity.User;

/**
 * Builds a dynamic {@link Specification} for the admin user list/search
 * endpoint. Every method always returns a non-null Specification — an
 * always-true (conjunction) predicate when its filter value is absent — so
 * callers can unconditionally chain them with {@code .and(...)}.
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> fullNameOrEmailContains(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern));
    }

    public static Specification<User> hasEnabled(Boolean enabled) {
        return (root, query, cb) -> enabled == null ? cb.conjunction() : cb.equal(root.get("enabled"), enabled);
    }
}
