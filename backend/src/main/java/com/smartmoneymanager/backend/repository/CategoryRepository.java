package com.smartmoneymanager.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.enums.CategoryType;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Every category a user can see: the global defaults (user IS NULL) plus their own custom ones. */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.type, c.name")
    List<Category> findAllVisibleToUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND c.type = :type ORDER BY c.name")
    List<Category> findAllVisibleToUserByType(@Param("userId") Long userId, @Param("type") CategoryType type);

    boolean existsByNameAndTypeAndUserIsNull(String name, CategoryType type);

    Optional<Category> findByNameAndTypeAndUserIsNull(String name, CategoryType type);
}
