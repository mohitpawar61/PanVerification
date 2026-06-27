package com.verify.panverification.util;

import com.verify.panverification.entity.PanVerification;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ExcelGenerator {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER  = "USER";

    /**
     * Generate a styled Excel workbook.
     *
     * @param data     records (pre-filtered by caller for USER role)
     * @param role     "ADMIN" or "USER"
     * @param username display name used in the cover sheet header
     */
    public ByteArrayInputStream generate(List<PanVerification> data,
                                         String role,
                                         String username) throws Exception {

        log.info("Excel generation started | role={} user={} records={}", role, username, data.size());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Styles ───────────────────────────────────────────────────────
            Styles s = new Styles(wb);

            // ── Sheet 1: Report ──────────────────────────────────────────────
            XSSFSheet sheet = wb.createSheet("PAN Report");
            sheet.setDisplayGridlines(false);
            sheet.createFreezePane(0, 6);   // freeze above data rows

            // Column widths
            boolean isAdmin = ROLE_ADMIN.equalsIgnoreCase(role);
            sheet.setColumnWidth(0,  4 * 256);    // #
            if (isAdmin) {
                sheet.setColumnWidth(1, 22 * 256);  // User
                sheet.setColumnWidth(2, 22 * 256);  // PAN
                sheet.setColumnWidth(3, 14 * 256);  // Status
                sheet.setColumnWidth(4, 20 * 256);  // Verified At
            } else {
                sheet.setColumnWidth(1, 26 * 256);  // PAN
                sheet.setColumnWidth(2, 14 * 256);  // Status
                sheet.setColumnWidth(3, 20 * 256);  // Verified At
            }

            int cols = isAdmin ? 5 : 4;

            // ── Banner (rows 0-2) ────────────────────────────────────────────
            buildBannerRow(sheet, wb, s, cols - 1, role, username);

            // ── Summary bar (row 3) ──────────────────────────────────────────
            buildSummaryRow(sheet, s, data, cols);

            // Empty spacer row 4
            sheet.createRow(4);

            // ── Table header (row 5) ─────────────────────────────────────────
            Row hRow = sheet.createRow(5);
            hRow.setHeightInPoints(24);
            int col = 0;
            addHeaderCell(hRow, col++, "#",           s.th);
            if (isAdmin) addHeaderCell(hRow, col++, "Username",   s.th);
            addHeaderCell(hRow, col++, "PAN Number",  s.th);
            addHeaderCell(hRow, col++, "Status",      s.th);
            addHeaderCell(hRow, col++, "Verified At", s.th);

            // ── Data rows (from row 6) ───────────────────────────────────────
            long valid = 0, invalid = 0;
            int rowNum = 6;
            for (PanVerification p : data) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(20);

                boolean odd = (rowNum % 2 != 0);
                CellStyle base  = odd ? s.tdOdd  : s.tdEven;
                CellStyle right = odd ? s.tdOddR : s.tdEvenR;

                col = 0;
                createCell(row, col++, String.valueOf(rowNum - 5), right);
                if (isAdmin) createCell(row, col++, nvl(p.getUser().getUsername()), base);
                createCell(row, col++, nvl(p.getPanNumber()), base);

                // Status cell with colour
                String status = nvl(p.getPanStatus()).toUpperCase();
                CellStyle statusStyle = getStatusStyle(wb, s, status, odd);
                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(status);
                statusCell.setCellStyle(statusStyle);

                createCell(row, col++, formatDate(p.getVerifiedAt()), base);

                if ("VALID".equals(status))   valid++;
                if ("INVALID".equals(status)) invalid++;
                rowNum++;
            }

            // ── Sheet 2: Summary
            buildSummarySheet(wb, s, data.size(), valid, invalid, role, username);

            // ── Auto-filter on data
            if (data.size() > 0) {
                sheet.setAutoFilter(new CellRangeAddress(5, rowNum - 1, 0, cols - 1));
            }

            // ── Write output ─────────────────────────────────────────────────
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            log.info("Excel generation complete | rows={}", data.size());
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // Backward-compat overload
    public ByteArrayInputStream generate(List<PanVerification> data) throws Exception {
        return generate(data, ROLE_ADMIN, "Administrator");
    }


    // Banner

    private void buildBannerRow(XSSFSheet sheet, XSSFWorkbook wb, Styles s,
                                int lastCol, String role, String username) {

        // Merge rows 0-2 for banner
        for (int r = 0; r <= 2; r++) {
            Row row = sheet.createRow(r);
            row.setHeightInPoints(r == 1 ? 30 : 16);
            for (int c = 0; c <= lastCol; c++) {
                row.createCell(c).setCellStyle(s.banner);
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 2, 0, lastCol));

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        String scope = ROLE_ADMIN.equalsIgnoreCase(role) ? "All Users" : "User: " + username;
        sheet.getRow(0).getCell(0).setCellValue(
                "PAN Verification Report\nGenerated: " + ts + "   |   " + scope);
    }


    // Summary bar (row 3)

    private void buildSummaryRow(XSSFSheet sheet, Styles s,
                                 List<PanVerification> data, int totalCols) {

        long valid   = data.stream().filter(p -> "VALID".equalsIgnoreCase(p.getPanStatus())).count();
        long invalid = data.stream().filter(p -> "INVALID".equalsIgnoreCase(p.getPanStatus())).count();
        long pending = data.size() - valid - invalid;

        Row row = sheet.createRow(3);
        row.setHeightInPoints(36);

        // Each summary block spans a quarter of the available columns
        int span = Math.max(1, totalCols / 4);

        writeSummaryBlock(sheet, row, s.summTotal,  0,        span - 1, "TOTAL",   data.size());
        writeSummaryBlock(sheet, row, s.summValid,  span,     2*span-1, "VALID",   (int)valid);
        writeSummaryBlock(sheet, row, s.summInvalid, 2*span,  3*span-1, "INVALID", (int)invalid);
        writeSummaryBlock(sheet, row, s.summPending, 3*span,  totalCols-1, "PENDING",(int)pending);
    }

    private void writeSummaryBlock(XSSFSheet sheet, Row row, CellStyle cs,
                                   int startCol, int endCol, String label, int value) {
        for (int c = startCol; c <= endCol; c++) row.createCell(c).setCellStyle(cs);
        row.getCell(startCol).setCellValue(value + " " + label);
        if (startCol < endCol)
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
    }


    // Summary sheet

    private void buildSummarySheet(XSSFWorkbook wb, Styles s,
                                   int total, long valid, long invalid,
                                   String role, String username) {

        XSSFSheet ss = wb.createSheet("Summary");
        ss.setDisplayGridlines(false);
        ss.setColumnWidth(0, 24 * 256);
        ss.setColumnWidth(1, 14 * 256);

        // Title
        Row t = ss.createRow(0); t.setHeightInPoints(30);
        Cell tc = t.createCell(0); tc.setCellValue("Summary Report"); tc.setCellStyle(s.banner);
        ss.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        // Info rows
        String[][] rows = {
                {"Role",         role},
                {"User",         username},
                {"Total Records",String.valueOf(total)},
                {"Valid",        String.valueOf(valid)},
                {"Invalid",      String.valueOf(invalid)},
                {"Pending",      String.valueOf(total - valid - invalid)},
                {"Generated At", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}
        };

        int rn = 2;
        for (String[] pair : rows) {
            Row r  = ss.createRow(rn++);
            r.setHeightInPoints(20);
            Cell lc = r.createCell(0); lc.setCellValue(pair[0]); lc.setCellStyle(s.th);
            Cell vc = r.createCell(1); vc.setCellValue(pair[1]); vc.setCellStyle(s.tdOdd);
        }
    }


    // Cell helpers
    private void addHeaderCell(Row row, int col, String value, CellStyle cs) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(cs);
    }

    private void createCell(Row row, int col, String value, CellStyle cs) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(cs);
    }

    private CellStyle getStatusStyle(XSSFWorkbook wb, Styles s,
                                     String status, boolean odd) {
        XSSFCellStyle cs = wb.createCellStyle();
        cs.cloneStyleFrom(odd ? s.tdOdd : s.tdEven);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setFont(s.boldFont(wb));

        XSSFColor color;
        switch (status) {
            case "VALID":
                color = xColor(22, 163, 74); break;
            case "INVALID":
                color = xColor(220, 38, 38); break;
            default:
                color = xColor(161, 98, 7);  break;
        }
        cs.setFont(colorFont(wb, color));
        return cs;
    }


    // Styles inner class
    static class Styles {
        CellStyle banner, th;
        CellStyle tdOdd, tdEven, tdOddR, tdEvenR;
        CellStyle summTotal, summValid, summInvalid, summPending;

        Styles(XSSFWorkbook wb) {
            banner      = buildBanner(wb);
            th          = buildTh(wb);
            tdOdd       = buildTd(wb, true,  false);
            tdEven      = buildTd(wb, false, false);
            tdOddR      = buildTd(wb, true,  true);
            tdEvenR     = buildTd(wb, false, true);
            summTotal   = buildSumm(wb, 30,  64, 175);
            summValid   = buildSumm(wb, 22, 163,  74);
            summInvalid = buildSumm(wb, 220,  38,  38);
            summPending = buildSumm(wb, 161,  98,   7);
        }

        XSSFFont boldFont(XSSFWorkbook wb) {
            XSSFFont f = wb.createFont();
            f.setBold(true);
            return f;
        }

        private CellStyle buildBanner(XSSFWorkbook wb) {
            XSSFCellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(xColor(30, 64, 175));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            cs.setWrapText(true);
            XSSFFont f = wb.createFont();
            f.setBold(true); f.setFontHeightInPoints((short) 16);
            f.setColor(xColor(255, 255, 255));
            cs.setFont(f);
            return cs;
        }

        private CellStyle buildTh(XSSFWorkbook wb) {
            XSSFCellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(xColor(30, 64, 175));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.CENTER);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(cs, BorderStyle.THIN, xColor(30, 64, 175));
            XSSFFont f = wb.createFont();
            f.setBold(true); f.setFontHeightInPoints((short) 10);
            f.setColor(xColor(255, 255, 255));
            cs.setFont(f); return cs;
        }

        private CellStyle buildTd(XSSFWorkbook wb, boolean odd, boolean right) {
            XSSFCellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(odd ? xColor(239, 246, 255) : xColor(255, 255, 255));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(cs, BorderStyle.THIN, xColor(191, 219, 254));
            XSSFFont f = wb.createFont();
            f.setFontHeightInPoints((short) 9);
            f.setColor(xColor(15, 23, 42));
            cs.setFont(f); return cs;
        }

        private CellStyle buildSumm(XSSFWorkbook wb, int r, int g, int b) {
            XSSFCellStyle cs = wb.createCellStyle();
            // Light tint background
            cs.setFillForegroundColor(xColor(
                    Math.min(r + 210, 255), Math.min(g + 210, 255), Math.min(b + 210, 255)));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.CENTER);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(cs, BorderStyle.MEDIUM, xColor(r, g, b));
            XSSFFont f = wb.createFont();
            f.setBold(true); f.setFontHeightInPoints((short) 11);
            f.setColor(xColor(r, g, b));
            cs.setFont(f); return cs;
        }

        private void setBorder(XSSFCellStyle cs, BorderStyle bs, XSSFColor color) {
            cs.setBorderTop(bs);    cs.setTopBorderColor(color);
            cs.setBorderBottom(bs); cs.setBottomBorderColor(color);
            cs.setBorderLeft(bs);   cs.setLeftBorderColor(color);
            cs.setBorderRight(bs);  cs.setRightBorderColor(color);
        }
    }


    // Utilities
    private static XSSFColor xColor(int r, int g, int b) {
        return new XSSFColor(new java.awt.Color(r, g, b), null);
    }

    private static XSSFFont colorFont(XSSFWorkbook wb, XSSFColor color) {
        XSSFFont f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 9);
        f.setColor(color); return f;
    }

    private String nvl(String s)                         { return s != null ? s : "-"; }
    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}