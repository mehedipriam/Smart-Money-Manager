package com.smartmoneymanager.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.dto.response.CategoryResponse;
import com.smartmoneymanager.backend.dto.response.ImportPreviewResponse;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.enums.CategoryType;
import com.smartmoneymanager.backend.entity.enums.ImportSource;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.service.CategoryService;
import com.smartmoneymanager.backend.service.importing.ImportParseResult;
import com.smartmoneymanager.backend.service.importing.ImportParser;
import com.smartmoneymanager.backend.service.importing.ImportParserRegistry;
import com.smartmoneymanager.backend.service.importing.NormalizedTransactionRow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the orchestration logic only — parsing itself is {@link
 * com.smartmoneymanager.backend.service.importing.parser.DarazSalesTrackerParserTest}'s job.
 * The key guarantee tested here: preview never writes anything.
 */
@ExtendWith(MockitoExtension.class)
class ImportPreviewServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ImportParserRegistry parserRegistry;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ImportParser parser;
    @Mock
    private MultipartFile file;

    @InjectMocks
    private ImportPreviewServiceImpl service;

    @BeforeEach
    void setUp() {
        when(parserRegistry.detect(file)).thenReturn(parser);
        when(parser.getSource()).thenReturn(ImportSource.DARAZ_SALES_TRACKER);
        when(parser.getSuggestedCategoryName()).thenReturn("Daraz Sell");
    }

    private NormalizedTransactionRow incomeRow() {
        return new NormalizedTransactionRow(
                LocalDate.of(2026, 8, 7), "Widget (Order #111)", TransactionType.INCOME, new BigDecimal("13.55"), "BDT", "111");
    }

    @Test
    void suggestsAnExistingCategoryWhenOneMatchesByName() {
        when(parser.parse(file)).thenReturn(new ImportParseResult(List.of(incomeRow()), 1, 0, List.of()));
        when(transactionRepository.count(anySpec())).thenReturn(0L);
        CategoryResponse existing = CategoryResponse.builder().id(9L).name("Daraz Sell").type("INCOME").build();
        when(categoryService.getCategories(USER_ID, CategoryType.INCOME)).thenReturn(List.of(existing));

        ImportPreviewResponse response = service.preview(USER_ID, file);

        assertThat(response.getSuggestedIncomeCategoryId()).isEqualTo(9L);
        assertThat(response.getSuggestedExpenseCategoryId()).isNull();
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void warnsInsteadOfCreatingWhenNoMatchingCategoryExists() {
        when(parser.parse(file)).thenReturn(new ImportParseResult(List.of(incomeRow()), 1, 0, List.of()));
        when(transactionRepository.count(anySpec())).thenReturn(0L);
        when(categoryService.getCategories(USER_ID, CategoryType.INCOME)).thenReturn(List.of());

        ImportPreviewResponse response = service.preview(USER_ID, file);

        assertThat(response.getSuggestedIncomeCategoryId()).isNull();
        assertThat(response.getWarnings()).anyMatch(w -> w.contains("Daraz Sell"));
    }

    @Test
    void flagsARowAsDuplicateWhenAMatchingTransactionAlreadyExists() {
        when(parser.parse(file)).thenReturn(new ImportParseResult(List.of(incomeRow()), 1, 0, List.of()));
        when(transactionRepository.count(anySpec())).thenReturn(1L);
        when(categoryService.getCategories(USER_ID, CategoryType.INCOME)).thenReturn(List.of());

        ImportPreviewResponse response = service.preview(USER_ID, file);

        assertThat(response.getRows()).hasSize(1);
        assertThat(response.getRows().get(0).isDuplicate()).isTrue();
        assertThat(response.getDuplicateCount()).isEqualTo(1);
    }

    @Test
    void previewNeverWritesAnyTransaction() {
        when(parser.parse(file)).thenReturn(new ImportParseResult(List.of(incomeRow()), 1, 0, List.of()));
        when(transactionRepository.count(anySpec())).thenReturn(0L);
        when(categoryService.getCategories(USER_ID, CategoryType.INCOME)).thenReturn(List.of());

        service.preview(USER_ID, file);

        verify(transactionRepository, never()).save(any());
        verify(transactionRepository, never()).saveAndFlush(any());
        verify(transactionRepository, never()).deleteById(any());
    }

    /** Pins the inferred type of {@code any()} to {@code Specification<Transaction>} — needed because
     *  this Spring Data version overloads {@code count}/{@code delete} with a {@code PredicateSpecification}
     *  variant too, which makes a bare {@code any()} ambiguous at the call site. */
    private static Specification<Transaction> anySpec() {
        return any();
    }
}
