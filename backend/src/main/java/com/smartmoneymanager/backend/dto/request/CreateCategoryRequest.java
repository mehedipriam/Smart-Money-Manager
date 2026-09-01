package com.smartmoneymanager.backend.dto.request;

import com.smartmoneymanager.backend.entity.enums.CategoryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    @Size(max = 20, message = "Color must be at most 20 characters")
    private String color;
}
