package com.smartmoneymanager.backend.service.importing.parser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.entity.enums.ImportSource;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.service.importing.ImportParseResult;
import com.smartmoneymanager.backend.service.importing.ImportParser;
import com.smartmoneymanager.backend.service.importing.NormalizedTransactionRow;

/**
 * Recognizes the "Daraz Sales Tracker" export template: an {@code Orders} sheet with a fixed set
 * of headers. Only a delivered order with a real, numeric "Profit (BDT)" becomes a transaction —
 * everything else (pending/cancelled/SLA-breached/blank-profit rows) is money that never actually
 * moved, so it's skipped rather than logged.
 */
@Component
public class DarazSalesTrackerParser implements ImportParser {

    private static final String SUGGESTED_CATEGORY_NAME = "Daraz Sell";
    private static final String SHEET_NAME = "Orders";
    private static final int MAX_DATA_ROWS = 2000;
    private static final String CURRENCY = "BDT";

    private static final String COL_DATE = "Date";
    private static final String COL_ORDER_NUMBER = "Order Number";
    private static final String COL_PRODUCT = "Product";
    private static final String COL_STATUS = "Status";
    private static final String COL_PROFIT_BDT = "Profit (BDT)";

    private static final Set<String> REQUIRED_HEADERS =
            Set.of(COL_DATE, COL_ORDER_NUMBER, COL_PRODUCT, COL_STATUS, COL_PROFIT_BDT);

    @Override
    public ImportSource getSource() {
        return ImportSource.DARAZ_SALES_TRACKER;
    }

    @Override
    public String getSuggestedCategoryName() {
        return SUGGESTED_CATEGORY_NAME;
    }

    @Override
    public boolean supports(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            return sheet != null && headerIndex(sheet) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ImportParseResult parse(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new InvalidOperationException(
                        "Expected a sheet named \"" + SHEET_NAME + "\" but it was not found in this file.");
            }
            Map<String, Integer> headers = headerIndex(sheet);
            if (headers == null) {
                Set<String> present = headerNamesPresent(sheet);
                List<String> missing = REQUIRED_HEADERS.stream().filter(h -> !present.contains(h)).toList();
                throw new InvalidOperationException("Missing required column(s) in the \"" + SHEET_NAME + "\" sheet: " + missing);
            }
            return parseRows(sheet, headers);
        } catch (IOException e) {
            throw new InvalidOperationException("Could not read the uploaded file");
        }
    }

    /** Null if the header row is missing any required column. */
    private Map<String, Integer> headerIndex(Sheet sheet) {
        Set<String> present = headerNamesPresent(sheet);
        if (!present.containsAll(REQUIRED_HEADERS)) {
            return null;
        }
        Row headerRow = sheet.getRow(0);
        DataFormatter formatter = new DataFormatter();
        Map<String, Integer> index = new HashMap<>();
        for (Cell cell : headerRow) {
            String name = formatter.formatCellValue(cell).trim();
            if (!name.isEmpty()) {
                index.put(name, cell.getColumnIndex());
            }
        }
        return index;
    }

    private Set<String> headerNamesPresent(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return Set.of();
        }
        DataFormatter formatter = new DataFormatter();
        Set<String> names = new java.util.HashSet<>();
        for (Cell cell : headerRow) {
            String name = formatter.formatCellValue(cell).trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    private ImportParseResult parseRows(Sheet sheet, Map<String, Integer> headers) {
        DataFormatter formatter = new DataFormatter();
        List<NormalizedTransactionRow> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int totalDetected = 0;
        int skippedRowCount = 0;

        int lastRow = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
        for (int r = 1; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null || isRowBlank(row, headers)) {
                continue;
            }
            totalDetected++;
            String rowLabel = "Row " + (r + 1);

            String status = text(row, headers, COL_STATUS, formatter);
            boolean delivered = status != null && status.toLowerCase(Locale.ROOT).contains("delivered");
            if (!delivered) {
                skippedRowCount++;
                continue;
            }

            BigDecimal profit = numeric(row, headers, COL_PROFIT_BDT);
            if (profit == null) {
                skippedRowCount++;
                warnings.add(rowLabel + ": \"Profit (BDT)\" is missing or not a number — skipped");
                continue;
            }

            LocalDate date = date(row, headers, COL_DATE);
            if (date == null) {
                skippedRowCount++;
                warnings.add(rowLabel + ": could not read a valid Date — skipped");
                continue;
            }

            String orderNumber = text(row, headers, COL_ORDER_NUMBER, formatter);
            if (orderNumber == null) {
                skippedRowCount++;
                warnings.add(rowLabel + ": missing Order Number — skipped");
                continue;
            }

            String product = text(row, headers, COL_PRODUCT, formatter);
            if (product == null) {
                skippedRowCount++;
                warnings.add(rowLabel + " (Order #" + orderNumber + "): missing Product — skipped");
                continue;
            }

            TransactionType type = profit.signum() >= 0 ? TransactionType.INCOME : TransactionType.EXPENSE;
            BigDecimal amount = profit.abs();
            String description = truncate(product + " (Order #" + orderNumber + ")", 255);

            rows.add(new NormalizedTransactionRow(date, description, type, amount, CURRENCY, orderNumber));
        }

        return new ImportParseResult(rows, totalDetected, skippedRowCount, warnings);
    }

    private boolean isRowBlank(Row row, Map<String, Integer> headers) {
        DataFormatter formatter = new DataFormatter();
        for (String column : REQUIRED_HEADERS) {
            String value = text(row, headers, column, formatter);
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    private String text(Row row, Map<String, Integer> headers, String column, DataFormatter formatter) {
        Integer col = headers.get(column);
        if (col == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal numeric(Row row, Map<String, Integer> headers, String column) {
        Integer col = headers.get(column);
        if (col == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        if (type == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (type == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private LocalDate date(Row row, Map<String, Integer> headers, String column) {
        Integer col = headers.get(column);
        if (col == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (type == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
