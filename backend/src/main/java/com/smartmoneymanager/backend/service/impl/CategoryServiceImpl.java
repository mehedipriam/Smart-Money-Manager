package com.smartmoneymanager.backend.service.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmoneymanager.backend.dto.request.CreateCategoryRequest;
import com.smartmoneymanager.backend.dto.request.UpdateCategoryRequest;
import com.smartmoneymanager.backend.dto.response.CategoryResponse;
import com.smartmoneymanager.backend.entity.Category;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.exception.ResourceInUseException;
import com.smartmoneymanager.backend.exception.ResourceNotFoundException;
import com.smartmoneymanager.backend.mapper.CategoryMapper;
import com.smartmoneymanager.backend.repository.CategoryRepository;
import com.smartmoneymanager.backend.repository.UserRepository;
import com.smartmoneymanager.backend.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(Long userId, CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findAllVisibleToUser(userId)
                : categoryRepository.findAllVisibleToUserByType(userId, type);
        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    public CategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
        Category category = Category.builder()
                .user(userRepository.getReferenceById(userId))
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .color(request.getColor())
                .isDefault(false)
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        Category category = findEditableOwnCategory(userId, categoryId);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = findEditableOwnCategory(userId, categoryId);
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("Cannot delete a category that is used by transactions, budgets, or bills");
        }
    }

    private Category findEditableOwnCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (category.isDefault()) {
            throw new InvalidOperationException("Default categories cannot be edited or deleted");
        }
        // Custom category owned by someone else — respond as "not found" rather than
        // "forbidden" so a probing user can't tell the category exists at all.
        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }
}
