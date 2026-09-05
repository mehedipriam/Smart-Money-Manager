package com.smartmoneymanager.backend.dto.response;

import java.util.List;

import com.smartmoneymanager.backend.entity.enums.ImportSource;

import lombok.Builder;
import lombok.Getter;

/**
 * Read-only result of parsing an uploaded file. Nothing about handling this response ever writes
 * a transaction — the frontend still submits each row it keeps through the normal
 * {@code POST /api/transactions} flow.
 */
@Getter
@Builder
public class ImportPreviewResponse {
    private final ImportSource source;
    private final List<ImportRowResponse> rows;
    private final List<String> warnings;
    private final int skippedRowCount;
    private final int totalDetected;
    private final int validRowCount;
    private final int duplicateCount;
    /** Null when no matching category exists yet — the frontend must then require an explicit pick. */
    private final Long suggestedIncomeCategoryId;
    private final Long suggestedExpenseCategoryId;
    private final String summary;
}
