package com.smartmoneymanager.backend.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.dto.response.ImportPreviewResponse;
import com.smartmoneymanager.backend.dto.response.ImportRowResponse;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.service.CategoryService;
import com.smartmoneymanager.backend.service.ImportPreviewService;
import com.smartmoneymanager.backend.service.importing.ImportParseResult;
import com.smartmoneymanager.backend.service.importing.ImportParser;
import com.smartmoneymanager.backend.service.importing.ImportParserRegistry;
import com.smartmoneymanager.backend.service.importing.NormalizedTransactionRow;
import com.smartmoneymanager.backend.specification.TransactionSpecifications;

import lombok.RequiredArgsConstructor;

/**
 * Orchestrates a preview: detect source -> parse -> flag likely duplicates -> suggest (never
 * create) categories. Deliberately knows nothing about any particular source's file format —
 * that's entirely inside whichever {@link ImportParser} the registry picked.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImportPreviewServiceImpl implements ImportPreviewService {

    private final ImportParserRegistry parserRegistry;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Override
    public ImportPreviewResponse preview(Long userId, MultipartFile file) {
        ImportParser parser = parserRegistry.detect(file);
        ImportParseResult result = parser.parse(file);

        List<ImportRowResponse> rows = new ArrayList<>();
        int duplicateCount = 0;
        boolean hasIncome = false;
        boolean hasExpense = false;

        for (NormalizedTransactionRow row : result.rows()) {
            boolean duplicate = isLikelyDuplicate(userId, row);
            if (duplicate) {
                duplicateCount++;
            }
            hasIncome = hasIncome || row.type() == TransactionType.INCOME;
            hasExpense = hasExpense || row.type() == TransactionType.EXPENSE;

            rows.add(ImportRowResponse.builder()
                    .transactionDate(row.transactionDate())
                    .description(row.description())
                    .type(row.type())
                    .amount(row.amount())
                    .currency(row.currency())
                    .externalReference(row.externalReference())
                    .duplicate(duplicate)
                    .build());
        }

        List<String> warnings = new ArrayList<>(result.warnings());
        String categoryName = parser.getSuggestedCategoryName();
        Long suggestedIncomeCategoryId = hasIncome ? resolveCategoryOrWarn(userId, CategoryType.INCOME, categoryName, warnings) : null;
        Long suggestedExpenseCategoryId = hasExpense ? resolveCategoryOrWarn(userId, CategoryType.EXPENSE, categoryName, warnings) : null;

        return ImportPreviewResponse.builder()
                .source(parser.getSource())
                .rows(rows)
                .warnings(warnings)
                .skippedRowCount(result.skippedRowCount())
                .totalDetected(result.totalDetected())
                .validRowCount(rows.size())
                .duplicateCount(duplicateCount)
                .suggestedIncomeCategoryId(suggestedIncomeCategoryId)
                .suggestedExpenseCategoryId(suggestedExpenseCategoryId)
                .summary(buildSummary(rows.size(), result.skippedRowCount(), duplicateCount))
                .build();
    }

    private boolean isLikelyDuplicate(Long userId, NormalizedTransactionRow row) {
        Specification<Transaction> spec = Specification
                .where(TransactionSpecifications.belongsToUser(userId))
                .and(TransactionSpecifications.hasType(row.type()))
                .and(TransactionSpecifications.dateFrom(row.transactionDate()))
                .and(TransactionSpecifications.dateTo(row.transactionDate()))
                .and(TransactionSpecifications.amountFrom(row.amount()))
                .and(TransactionSpecifications.amountTo(row.amount()))
                .and(TransactionSpecifications.descriptionOrNoteContains(row.externalReference()));
        return transactionRepository.count(spec) > 0;
    }

    private Long resolveCategoryOrWarn(Long userId, CategoryType type, String name, List<String> warnings) {
        Long id = categoryService.getCategories(userId, type).stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .map(c -> c.getId())
                .findFirst()
                .orElse(null);
        if (id == null) {
            warnings.add("No existing \"" + name + "\" " + type.name().toLowerCase() + " category found — choose one or create it on the Categories page first.");
        }
        return id;
    }

    private String buildSummary(int validCount, int skippedCount, int duplicateCount) {
        StringBuilder sb = new StringBuilder()
                .append(validCount)
                .append(validCount == 1 ? " valid transaction found" : " valid transactions found");
        if (skippedCount > 0) {
            sb.append(". ").append(skippedCount).append(skippedCount == 1 ? " row skipped" : " rows skipped");
        }
        if (duplicateCount > 0) {
            sb.append(". ").append(duplicateCount)
                    .append(duplicateCount == 1 ? " row looks like a duplicate" : " rows look like duplicates");
        }
        return sb.append(".").toString();
    }
}
