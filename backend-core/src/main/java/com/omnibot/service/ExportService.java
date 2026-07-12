package com.omnibot.service;

import com.omnibot.model.Order;
import com.omnibot.model.PaymentTransaction;
import com.omnibot.repository.OrderRepository;
import com.omnibot.repository.PaymentTransactionRepository;

import com.lowagie.text.Document;
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentRepository;

    public ExportService(OrderRepository orderRepository,
                         PaymentTransactionRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    // ─────────────────────────────────────────────
    //  Excel Export
    // ─────────────────────────────────────────────
    public byte[] generateExcel(Long userId) throws IOException {
        List<Order> orders = orderRepository.findByUserId(userId);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Styles ──────────────────────────────────────────────────────
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)79, (byte)70, (byte)229}, null)); // Indigo
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle amountStyle = workbook.createCellStyle();
            XSSFDataFormat dataFormat = workbook.createDataFormat();
            amountStyle.setDataFormat(dataFormat.getFormat("$#,##0.00"));

            XSSFCellStyle currencyGreenStyle = workbook.createCellStyle();
            XSSFFont greenFont = workbook.createFont();
            greenFont.setColor(new XSSFColor(new byte[]{(byte)16, (byte)185, (byte)129}, null)); // Emerald
            greenFont.setBold(true);
            currencyGreenStyle.setFont(greenFont);
            currencyGreenStyle.setDataFormat(dataFormat.getFormat("$#,##0.00"));

            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);

            XSSFCellStyle altRowStyle = workbook.createCellStyle();
            altRowStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)238, (byte)242, (byte)255}, null));
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Transactions Sheet ───────────────────────────────────────────
            XSSFSheet sheet = workbook.createSheet("Transactions");

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("OmniBot – Monthly Expense Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Subtitle
            Row subtitleRow = sheet.createRow(1);
            subtitleRow.createCell(0).setCellValue("Generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // Blank row
            sheet.createRow(2);

            // Header Row
            String[] columns = {"Order ID", "Vendor", "Category", "Status", "Amount (USD)", "Currency", "Date"};
            Row headerRow = sheet.createRow(3);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowNum = 4;
            BigDecimal totalSpent = BigDecimal.ZERO;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                if (rowNum % 2 == 0) {
                    for (int c = 0; c < columns.length; c++) row.createCell(c).setCellStyle(altRowStyle);
                }

                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getVendorId());
                row.createCell(2).setCellValue(order.getCategory());
                row.createCell(3).setCellValue(order.getStatus());
                Cell amtCell = row.createCell(4);
                amtCell.setCellValue(order.getTotalAmount().doubleValue());
                amtCell.setCellStyle(amountStyle);
                row.createCell(5).setCellValue(order.getCurrency());
                row.createCell(6).setCellValue(order.getCreatedAt() != null ? order.getCreatedAt().format(FMT) : "");
                totalSpent = totalSpent.add(order.getTotalAmount());
            }

            // Totals Row
            Row totalRow = sheet.createRow(rowNum + 1);
            XSSFCellStyle totalLabelStyle = workbook.createCellStyle();
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalLabelStyle.setFont(boldFont);
            Cell totalLabelCell = totalRow.createCell(3);
            totalLabelCell.setCellValue("TOTAL SPENT:");
            totalLabelCell.setCellStyle(totalLabelStyle);
            Cell totalCell = totalRow.createCell(4);
            totalCell.setCellValue(totalSpent.doubleValue());
            totalCell.setCellStyle(currencyGreenStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ── Summary Sheet ────────────────────────────────────────────────
            XSSFSheet summarySheet = workbook.createSheet("Category Summary");
            Row sumTitle = summarySheet.createRow(0);
            Cell sumTitleCell = sumTitle.createCell(0);
            sumTitleCell.setCellValue("Spending by Category");
            sumTitleCell.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            String[] sumHeaders = {"Category", "Order Count", "Total Spent"};
            Row sumHeader = summarySheet.createRow(2);
            for (int i = 0; i < sumHeaders.length; i++) {
                Cell c = sumHeader.createCell(i);
                c.setCellValue(sumHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            Map<String, List<Order>> byCategory = orders.stream()
                    .collect(Collectors.groupingBy(Order::getCategory));

            int sumRow = 3;
            for (Map.Entry<String, List<Order>> entry : byCategory.entrySet()) {
                BigDecimal catTotal = entry.getValue().stream()
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                Row row = summarySheet.createRow(sumRow++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().size());
                Cell c = row.createCell(2);
                c.setCellValue(catTotal.doubleValue());
                c.setCellStyle(amountStyle);
            }

            for (int i = 0; i < sumHeaders.length; i++) summarySheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─────────────────────────────────────────────
    //  PDF Export
    // ─────────────────────────────────────────────
    public byte[] generatePdf(Long userId) throws IOException {
        List<Order> orders = orderRepository.findByUserId(userId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Fonts
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new java.awt.Color(79, 70, 229));
        com.lowagie.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
        com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        com.lowagie.text.Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.DARK_GRAY);
        com.lowagie.text.Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.BLACK);
        com.lowagie.text.Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new java.awt.Color(16, 185, 129));

        // Title
        Paragraph title = new Paragraph("OmniBot — Expense Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4f);
        doc.add(title);

        Paragraph subtitle = new Paragraph("Generated on " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20f);
        doc.add(subtitle);

        // Transactions Table
        Paragraph tableTitle = new Paragraph("Transaction History", boldBodyFont);
        tableTitle.setSpacingAfter(6f);
        doc.add(tableTitle);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 3f});

        java.awt.Color indigo = new java.awt.Color(79, 70, 229);
        java.awt.Color lightIndigo = new java.awt.Color(238, 242, 255);

        String[] cols = {"ID", "Vendor", "Category", "Status", "Amount", "Date"};
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(indigo);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        BigDecimal totalSpent = BigDecimal.ZERO;
        boolean alt = false;
        for (Order o : orders) {
            java.awt.Color rowColor = alt ? lightIndigo : java.awt.Color.WHITE;
            addTableCell(table, String.valueOf(o.getId()), bodyFont, rowColor, Element.ALIGN_CENTER);
            addTableCell(table, o.getVendorId(), bodyFont, rowColor, Element.ALIGN_LEFT);
            addTableCell(table, o.getCategory(), bodyFont, rowColor, Element.ALIGN_LEFT);
            addTableCell(table, o.getStatus(), bodyFont, rowColor, Element.ALIGN_CENTER);
            addTableCell(table, "$" + o.getTotalAmount().toPlainString(), boldBodyFont, rowColor, Element.ALIGN_RIGHT);
            addTableCell(table, o.getCreatedAt() != null ? o.getCreatedAt().format(FMT) : "", bodyFont, rowColor, Element.ALIGN_LEFT);
            totalSpent = totalSpent.add(o.getTotalAmount());
            alt = !alt;
        }
        doc.add(table);

        // Total
        Paragraph totalPara = new Paragraph("\nTotal Spent: $" + totalSpent.toPlainString(), totalFont);
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        totalPara.setSpacingAfter(20f);
        doc.add(totalPara);

        // Category Summary
        Paragraph catTitle = new Paragraph("Spending by Category", boldBodyFont);
        catTitle.setSpacingBefore(10f);
        catTitle.setSpacingAfter(6f);
        doc.add(catTitle);

        PdfPTable catTable = new PdfPTable(3);
        catTable.setWidthPercentage(60f);
        catTable.setWidths(new float[]{3f, 2f, 2f});
        String[] catCols = {"Category", "Orders", "Total"};
        for (String col : catCols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(indigo);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            cell.setBorder(Rectangle.NO_BORDER);
            catTable.addCell(cell);
        }

        Map<String, List<Order>> byCategory = orders.stream()
                .collect(Collectors.groupingBy(Order::getCategory));
        alt = false;
        for (Map.Entry<String, List<Order>> entry : byCategory.entrySet()) {
            BigDecimal catTotal = entry.getValue().stream()
                    .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            java.awt.Color rowColor = alt ? lightIndigo : java.awt.Color.WHITE;
            addTableCell(catTable, entry.getKey(), bodyFont, rowColor, Element.ALIGN_LEFT);
            addTableCell(catTable, String.valueOf(entry.getValue().size()), bodyFont, rowColor, Element.ALIGN_CENTER);
            addTableCell(catTable, "$" + catTotal.toPlainString(), boldBodyFont, rowColor, Element.ALIGN_RIGHT);
            alt = !alt;
        }
        doc.add(catTable);

        doc.close();
        return out.toByteArray();
    }

    private void addTableCell(PdfPTable table, String text, com.lowagie.text.Font font,
                               java.awt.Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }
}
