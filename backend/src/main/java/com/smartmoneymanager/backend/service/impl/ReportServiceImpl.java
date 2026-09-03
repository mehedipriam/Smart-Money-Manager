package com.smartmoneymanager.backend.service.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartmoneymanager.backend.dto.projection.TransactionAmountProjection;
import com.smartmoneymanager.backend.dto.request.DashboardRangeType;
import com.smartmoneymanager.backend.dto.response.CashFlowPointResponse;
import com.smartmoneymanager.backend.dto.response.ReportSummaryResponse;
import com.smartmoneymanager.backend.dto.response.SpendingByCategoryResponse;
import com.smartmoneymanager.backend.entity.Transaction;
import com.smartmoneymanager.backend.entity.enums.TransactionType;
import com.smartmoneymanager.backend.repository.TransactionRepository;
import com.smartmoneymanager.backend.service.ReportService;
import com.smartmoneymanager.backend.util.DateRangeUtils;
import com.smartmoneymanager.backend.util.FinancialCalculations;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    /** Above this many days, chart buckets switch from daily to monthly (mirrors the dashboard's rule). */
    private static final int DAILY_BUCKET_MAX_SPAN_DAYS = 31;
    /** Width of the fixed trend charts, in trailing calendar months, regardless of the selected report period. */
    private static final int TREND_MONTHS = 6;

    private final TransactionRepository transactionRepository;

    @Override
    public ReportSummaryResponse getReportSummary(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        LocalDate[] period = DateRangeUtils.resolveRange(range, customStart, customEnd, today);
        LocalDate periodStart = period[0];
        LocalDate periodEnd = period[1];

        long spanDays = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        LocalDate[] previousPeriod = DateRangeUtils.resolvePreviousRange(range, periodStart, periodEnd, spanDays);

        BigDecimal totalIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, periodStart, periodEnd);
        BigDecimal totalExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, periodStart, periodEnd);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        BigDecimal prevIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, previousPeriod[0], previousPeriod[1]);
        BigDecimal prevExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, previousPeriod[0], previousPeriod[1]);
        BigDecimal prevSavings = prevIncome.subtract(prevExpenses);

        List<SpendingByCategoryResponse> expenseByCategory = withPercentages(
                transactionRepository.findSpendingByCategory(userId, TransactionType.EXPENSE, periodStart, periodEnd), totalExpenses);
        List<SpendingByCategoryResponse> incomeByCategory = withPercentages(
                transactionRepository.findSpendingByCategory(userId, TransactionType.INCOME, periodStart, periodEnd), totalIncome);

        int monthsSpanned = (int) Period.between(periodStart.withDayOfMonth(1), periodEnd.withDayOfMonth(1)).toTotalMonths() + 1;

        return ReportSummaryResponse.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .savingsRate(savingsRate(netSavings, totalIncome))
                .incomeChangePercent(FinancialCalculations.percentChange(totalIncome, prevIncome))
                .expenseChangePercent(FinancialCalculations.percentChange(totalExpenses, prevExpenses))
                .savingsChangePercent(FinancialCalculations.percentChange(netSavings, prevSavings))
                .highestExpenseCategory(expenseByCategory.isEmpty() ? null : expenseByCategory.get(0))
                .averageMonthlyExpense(totalExpenses.divide(BigDecimal.valueOf(monthsSpanned), 2, RoundingMode.HALF_UP))
                .expenseByCategory(expenseByCategory)
                .incomeByCategory(incomeByCategory)
                .cashFlow(buildBuckets(userId, periodStart, periodEnd))
                .monthlyTrend(buildMonthlyTrend(userId, today))
                .build();
    }

    @Override
    public byte[] exportCsv(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        LocalDate[] period = DateRangeUtils.resolveRange(range, customStart, customEnd, today);
        LocalDate periodStart = period[0];
        LocalDate periodEnd = period[1];

        BigDecimal totalIncome = transactionRepository.sumAmount(userId, TransactionType.INCOME, periodStart, periodEnd);
        BigDecimal totalExpenses = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, periodStart, periodEnd);
        List<Transaction> transactions = transactionRepository.findForExport(userId, periodStart, periodEnd);

        StringBuilder csv = new StringBuilder();
        csv.append("Smart Money Manager - Financial Report\n");
        csv.append("Period,").append(periodStart).append(" to ").append(periodEnd).append('\n');
        csv.append("Total Income,").append(totalIncome).append('\n');
        csv.append("Total Expenses,").append(totalExpenses).append('\n');
        csv.append("Net Savings,").append(totalIncome.subtract(totalExpenses)).append('\n');
        csv.append('\n');
        csv.append("Date,Type,Category,Account,Description,Amount\n");
        for (Transaction t : transactions) {
            csv.append(t.getTransactionDate()).append(',')
                    .append(t.getType()).append(',')
                    .append(csvField(t.getCategory().getName())).append(',')
                    .append(csvField(t.getAccount().getAccountName())).append(',')
                    .append(csvField(t.getDescription() == null ? "" : t.getDescription())).append(',')
                    .append(t.getAmount()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportPdf(Long userId, DashboardRangeType range, LocalDate customStart, LocalDate customEnd) {
        ReportSummaryResponse summary = getReportSummary(userId, range, customStart, customEnd);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Smart Money Manager - Financial Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph period = new Paragraph("Period: " + summary.getPeriodStart() + " to " + summary.getPeriodEnd(), mutedFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(18);
            document.add(period);

            document.add(new Paragraph("Summary", sectionFont));
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(8);
            summaryTable.setSpacingAfter(18);
            addRow(summaryTable, "Total Income", summary.getTotalIncome().toPlainString(), normalFont);
            addRow(summaryTable, "Total Expenses", summary.getTotalExpenses().toPlainString(), normalFont);
            addRow(summaryTable, "Net Savings", summary.getNetSavings().toPlainString(), normalFont);
            addRow(summaryTable, "Savings Rate", summary.getSavingsRate() == null ? "N/A" : summary.getSavingsRate() + "%", normalFont);
            addRow(summaryTable, "Average Monthly Expense", summary.getAverageMonthlyExpense().toPlainString(), normalFont);
            addRow(summaryTable, "Highest Expense Category",
                    summary.getHighestExpenseCategory() == null ? "N/A" : summary.getHighestExpenseCategory().getCategoryName(), normalFont);
            document.add(summaryTable);

            document.add(new Paragraph("Expense by Category", sectionFont));
            document.add(buildCategoryTable(summary.getExpenseByCategory(), normalFont));

            document.add(new Paragraph("Income by Category", sectionFont));
            document.add(buildCategoryTable(summary.getIncomeByCategory(), normalFont));

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }

    private BigDecimal savingsRate(BigDecimal netSavings, BigDecimal totalIncome) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<SpendingByCategoryResponse> withPercentages(List<SpendingByCategoryResponse> items, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (SpendingByCategoryResponse item : items) {
                item.setPercentage(item.getAmount()
                        .divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
            }
        } else {
            items.forEach(item -> item.setPercentage(BigDecimal.ZERO));
        }
        return items;
    }

    private List<CashFlowPointResponse> buildBuckets(Long userId, LocalDate start, LocalDate end) {
        List<TransactionAmountProjection> transactions = transactionRepository.findAmountsInRange(userId, start, end);
        return FinancialCalculations.buildCashFlowBuckets(transactions, start, end, DAILY_BUCKET_MAX_SPAN_DAYS);
    }

    /** Always monthly-bucketed: a ~180-day span comfortably exceeds {@link #DAILY_BUCKET_MAX_SPAN_DAYS}. */
    private List<CashFlowPointResponse> buildMonthlyTrend(Long userId, LocalDate today) {
        LocalDate start = today.minusMonths(TREND_MONTHS - 1L).withDayOfMonth(1);
        return buildBuckets(userId, start, today);
    }

    private String csvField(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private void addRow(PdfPTable table, String label, String value, Font font) {
        table.addCell(borderlessCell(label, font));
        table.addCell(borderlessCell(value, font));
    }

    private PdfPCell borderlessCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private PdfPTable buildCategoryTable(List<SpendingByCategoryResponse> items, Font font) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        table.setSpacingAfter(18);
        try {
            table.setWidths(new float[] { 3, 2, 2 });
        } catch (DocumentException e) {
            // Column count always matches the widths array above; this can never actually happen.
            throw new IllegalStateException(e);
        }

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        table.addCell(headerCell("Category", headerFont));
        table.addCell(headerCell("Amount", headerFont));
        table.addCell(headerCell("Percentage", headerFont));

        if (items.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No data for this period", font));
            empty.setColspan(3);
            empty.setPadding(6);
            table.addCell(empty);
        } else {
            for (SpendingByCategoryResponse item : items) {
                String label = (item.getIcon() == null ? "" : item.getIcon() + " ") + item.getCategoryName();
                table.addCell(dataCell(label, font));
                table.addCell(dataCell(item.getAmount().toPlainString(), font));
                table.addCell(dataCell(item.getPercentage() + "%", font));
            }
        }
        return table;
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(230, 245, 235));
        return cell;
    }

    private PdfPCell dataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        return cell;
    }
}
