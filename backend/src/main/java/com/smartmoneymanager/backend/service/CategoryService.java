package com.smartmoneymanager.backend.service;

import java.util.List;

import com.smartmoneymanager.backend.dto.request.CreateCategoryRequest;
import com.smartmoneymanager.backend.dto.request.UpdateCategoryRequest;
import com.smartmoneymanager.backend.dto.response.CategoryResponse;
import com.smartmoneymanager.backend.entity.enums.CategoryType;

public interface CategoryService {

    List<CategoryResponse> getCategories(Long userId, CategoryType type);

    CategoryResponse createCategory(Long userId, CreateCategoryRequest request);

    CategoryResponse updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request);

    void deleteCategory(Long userId, Long categoryId);
}
