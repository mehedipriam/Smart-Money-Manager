package com.smartmoneymanager.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Type is intentionally not editable — it's fixed at creation so it can't drift out of sync with anything already tagged with this category. */
@Getter
@Setter
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String name;

    @Size(max = 50, message = "Icon must be at most 50 characters")
    private String icon;

    @Size(max = 20, message = "Color must be at most 20 characters")
    private String color;
}
