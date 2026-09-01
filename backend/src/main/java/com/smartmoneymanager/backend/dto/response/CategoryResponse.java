package com.smartmoneymanager.backend.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String type;
    private String icon;
    private String color;
    private boolean defaultCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
