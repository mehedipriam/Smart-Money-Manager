package com.smartmoneymanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Lightweight category reference embedded in transaction/recurring-transaction responses. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummaryResponse {

    private Long id;
    private String name;
    private String type;
    private String icon;
    private String color;
}
