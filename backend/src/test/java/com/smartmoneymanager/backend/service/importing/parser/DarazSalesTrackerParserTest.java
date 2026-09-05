package com.smartmoneymanager.backend.service.importing.parser;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.exception.InvalidOperationException;
import com.smartmoneymanager.backend.service.importing.ImportParseResult;
import com.smartmoneymanager.backend.service.importing.NormalizedTransactionRow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** No fixture file — every workbook is built in-memory to match (or deliberately not match) the real Daraz template. */
class DarazSalesTrackerParserTest {

    private static final String[] HEADERS = {
            "Date", "Order Number", "Customer", "Product", "SKU", "Qty",
            "Listed Price (BDT)", "Buyer Paid (BDT)", "Estimated Release (BDT)", "Commission (BDT)",
            "Status", "Location/Logistics", "Tracking Number", "Expected Delivery",
            "Product Cost (BDT)", "Profit", "Notes", "Profit (BDT)",
    };
    private static final int COL_DATE = 0;
    private static final int COL_ORDER_NUMBER = 1;
    private static final int COL_PRODUCT = 3;
    private static final int COL_STATUS = 10;
    private static final int COL_PROFIT_BDT = 17;

    private final DarazSalesTrackerParser parser = new DarazSalesTrackerParser();

    private Object[] darazRow(String date, String orderNumber, String product, String status, Object profitBdt) {
        Object[] row = new Object[HEADERS.length];
        row[COL_DATE] = date;
        row[COL_ORDER_NUMBER] = orderNumber;
        row[COL_PRODUCT] = product;
        row[COL_STATUS] = status;
        row[COL_PROFIT_BDT] = profitBdt;
        return row;
    }

    private MultipartFile workbookFile(String sheetName, String[] headers, Object[][] dataRows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            if (headers != null) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }
            }
            for (int r = 0; r < dataRows.length; r++) {
                Row row = sheet.createRow(r + 1);
                Object[] values = dataRows[r];
                for (int c = 0; c < values.length; c++) {
                    Object value = values[c];
                    if (value == null) {
                        continue;
                    }
                    if (value instanceof String s) {
                        row.createCell(c).setCellValue(s);
                    } else if (value instanceof Number n) {
                        row.createCell(c).setCellValue(n.doubleValue());
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile("file", "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    @Test
    void recognizesTheCorrectWorkbook() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "Delivered", 13.55) });
        assertThat(parser.supports(file)).isTrue();
    }

    @Test
    void rejectsAWrongSheetName() throws Exception {
        MultipartFile file = workbookFile("Sheet1", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "Delivered", 13.55) });
        assertThat(parser.supports(file)).isFalse();
    }

    @Test
    void reportsMissingRequiredHeaders() throws Exception {
        String[] incompleteHeaders = { "Date", "Order Number", "Status" };
        MultipartFile file = workbookFile("Orders", incompleteHeaders, new Object[][] {});

        assertThat(parser.supports(file)).isFalse();
        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Product")
                .hasMessageContaining("Profit (BDT)");
    }

    @Test
    void deliveredPositiveProfitBecomesIncome() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "Delivered", 13.55) });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).hasSize(1);
        NormalizedTransactionRow row = result.rows().get(0);
        assertThat(row.type()).isEqualTo(TransactionType.INCOME);
        assertThat(row.amount()).isEqualByComparingTo(new BigDecimal("13.55"));
        assertThat(row.transactionDate()).isEqualTo(LocalDate.of(2026, 8, 7));
        assertThat(row.externalReference()).isEqualTo("111");
        assertThat(row.description()).isEqualTo("Widget (Order #111)");
    }

    @Test
    void deliveredNegativeProfitBecomesExpenseWithAbsoluteAmount() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-20", "222", "Gadget", "Delivered", -69.6) });

        NormalizedTransactionRow row = parser.parse(file).rows().get(0);

        assertThat(row.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(row.amount()).isEqualByComparingTo(new BigDecimal("69.6"));
    }

    @Test
    void statusCaseAndExtraWordsStillCountAsDelivered() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "delivered to hub", 10) });

        assertThat(parser.parse(file).rows()).hasSize(1);
    }

    @Test
    void nonDeliveredRowsAreSkippedSilentlyWithNoWarning() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "SLA Breached / Pending Cancellation", null) });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).isEmpty();
        assertThat(result.skippedRowCount()).isEqualTo(1);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void deliveredWithNonNumericProfitIsSkippedWithWarning() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", "Widget", "Delivered", "#VALUE!") });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).isEmpty();
        assertThat(result.skippedRowCount()).isEqualTo(1);
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0)).contains("Profit (BDT)");
    }

    @Test
    void deliveredWithInvalidDateIsSkippedWithWarning() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("not-a-date", "111", "Widget", "Delivered", 10) });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).isEmpty();
        assertThat(result.warnings().get(0)).contains("Date");
    }

    @Test
    void deliveredWithMissingProductIsSkippedWithWarning() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS,
                new Object[][] { darazRow("2026-08-07", "111", null, "Delivered", 10) });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).isEmpty();
        assertThat(result.warnings().get(0)).contains("Product");
    }

    @Test
    void multipleValidRowsAreAllReturnedNotJustTheFirst() throws Exception {
        MultipartFile file = workbookFile("Orders", HEADERS, new Object[][] {
                darazRow("2026-08-07", "111", "Widget", "Delivered", 13.55),
                darazRow("2026-08-08", "222", "Gadget", "SLA Breached", null),
                darazRow("2026-08-13", "333", "Thing", "Delivered", 194.35),
        });

        ImportParseResult result = parser.parse(file);

        assertThat(result.rows()).hasSize(2);
        assertThat(result.totalDetected()).isEqualTo(3);
        assertThat(result.skippedRowCount()).isEqualTo(1);
    }
}
