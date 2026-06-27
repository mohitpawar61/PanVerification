package com.verify.panverification.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.verify.panverification.entity.PanVerification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class PdfGenerator {

    // ── Brand Colors ──────────────────────────────────────────────────────────
    private static final BaseColor PRIMARY      = new BaseColor(30,  64, 175);   // deep blue
    private static final BaseColor ACCENT       = new BaseColor(59, 130, 246);   // blue-400
    private static final BaseColor SUCCESS      = new BaseColor(22, 163,  74);   // green-600
    private static final BaseColor DANGER       = new BaseColor(220,  38,  38);  // red-600
    private static final BaseColor WARN         = new BaseColor(234, 179,   8);  // yellow-500
    private static final BaseColor ROW_ODD      = new BaseColor(239, 246, 255);  // blue-50
    private static final BaseColor ROW_EVEN     = BaseColor.WHITE;
    private static final BaseColor HEADER_BG    = new BaseColor(30,  64, 175);
    private static final BaseColor BORDER_COLOR = new BaseColor(191, 219, 254);  // blue-200
    private static final BaseColor TEXT_MUTED   = new BaseColor(100, 116, 139);  // slate-500
    private static final BaseColor TEXT_DARK    = new BaseColor( 15,  23,  42);  // slate-900

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, PRIMARY);
    private static final Font FONT_SUB     = FontFactory.getFont(FontFactory.HELVETICA,       10, TEXT_MUTED);
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, PRIMARY);
    private static final Font FONT_TH      = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, BaseColor.WHITE);
    private static final Font FONT_TD      = FontFactory.getFont(FontFactory.HELVETICA,         9, TEXT_DARK);
    private static final Font FONT_BADGE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, BaseColor.WHITE);
    private static final Font FONT_FOOTER  = FontFactory.getFont(FontFactory.HELVETICA,         8, TEXT_MUTED);

    // ── Role constants ────────────────────────────────────────────────────────
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER  = "USER";

    /**
     * Generate a PAN Verification Report PDF.
     *
     * @param data     list of PanVerification records (pre-filtered by caller)
     * @param role     "ADMIN" or "USER"
     * @param username display name / username for the report header
     */
    public ByteArrayInputStream generate(List<PanVerification> data,
                                         String role,
                                         String username) throws Exception {

        log.info("PDF generation started | role={} user={} records={}", role, username, data.size());

        Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        writer.setPageEvent(new HeaderFooterEvent(role, username));

        doc.open();

        // ── Cover block ──────────────────────────────────────────────────────
        addCoverBlock(doc, role, username, data.size());

        // ── Summary bar ─────────────────────────────────────────────────────
        addSummaryBar(doc, data);

        doc.add(Chunk.NEWLINE);

        // ── Section heading ──────────────────────────────────────────────────
        Paragraph sectionHeading = new Paragraph("Verification Records", FONT_SECTION);
        sectionHeading.setSpacingBefore(8);
        sectionHeading.setSpacingAfter(6);
        doc.add(sectionHeading);

        // ── Data table ───────────────────────────────────────────────────────
        addDataTable(doc, data, role);

        doc.close();
        log.info("PDF generation complete | records={}", data.size());
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ── Convenience overload for backward-compat ──────────────────────────────
    public ByteArrayInputStream generate(List<PanVerification> data) throws Exception {
        return generate(data, ROLE_ADMIN, "Administrator");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void addCoverBlock(Document doc, String role, String username, int total) throws Exception {

        // Coloured header rectangle drawn via a table cell background
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(PRIMARY);
        cell.setPadding(20);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("PAN Verification Report", FONT_TITLE);
        title.setAlignment(Element.ALIGN_LEFT);
        Font whiteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
        title = new Paragraph("PAN Verification Report",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE));

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        Paragraph meta = new Paragraph(
                "Generated on " + ts + "   |   " +
                        (ROLE_ADMIN.equalsIgnoreCase(role) ? "All Users" : "User: " + username),
                FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(186, 230, 253)));

        cell.addElement(title);
        cell.addElement(meta);
        banner.addCell(cell);
        doc.add(banner);
        doc.add(Chunk.NEWLINE);
    }

    private void addSummaryBar(Document doc, List<PanVerification> data) throws Exception {
        long valid   = data.stream().filter(p -> "VALID".equalsIgnoreCase(p.getPanStatus())).count();
        long invalid = data.stream().filter(p -> "INVALID".equalsIgnoreCase(p.getPanStatus())).count();
        long pending = data.size() - valid - invalid;

        PdfPTable bar = new PdfPTable(new float[]{1, 1, 1, 1});
        bar.setWidthPercentage(100);
        bar.setSpacingBefore(4);

        addSummaryCell(bar, "Total",   String.valueOf(data.size()), PRIMARY);
        addSummaryCell(bar, "Valid",   String.valueOf(valid),       SUCCESS);
        addSummaryCell(bar, "Invalid", String.valueOf(invalid),     DANGER);
        addSummaryCell(bar, "Pending", String.valueOf(pending),     WARN);

        doc.add(bar);
    }

    private void addSummaryCell(PdfPTable table, String label, String value, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(1f);
        cell.setPadding(10);
        cell.setBackgroundColor(new BaseColor(
                Math.min(color.getRed()   + 200, 255),
                Math.min(color.getGreen() + 200, 255),
                Math.min(color.getBlue()  + 200, 255)));

        Paragraph val = new Paragraph(value,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, color));
        val.setAlignment(Element.ALIGN_CENTER);

        Paragraph lbl = new Paragraph(label,
                FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_MUTED));
        lbl.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(val);
        cell.addElement(lbl);
        table.addCell(cell);
    }

    private void addDataTable(Document doc, List<PanVerification> data, String role) throws Exception {

        // Columns differ by role: admin sees user info too
        boolean isAdmin = ROLE_ADMIN.equalsIgnoreCase(role);
        PdfPTable table = isAdmin
                ? new PdfPTable(new float[]{0.5f, 2f, 2.5f, 1.5f, 2f})
                : new PdfPTable(new float[]{0.5f, 2.5f, 1.5f, 2f});
        table.setWidthPercentage(100);

        // Header row
        addTh(table, "#");
        if (isAdmin) addTh(table, "User");
        addTh(table, "PAN Number");
        addTh(table, "Status");
        addTh(table, "Verified At");

        // Data rows
        int idx = 1;
        for (PanVerification p : data) {
            BaseColor bg = (idx % 2 == 0) ? ROW_EVEN : ROW_ODD;

            addTd(table, String.valueOf(idx), bg, Element.ALIGN_CENTER);
            if (isAdmin) addTd(table, nvl(p.getUser().getUsername()), bg, Element.ALIGN_LEFT);
            addTd(table, nvl(p.getPanNumber()), bg, Element.ALIGN_LEFT);
            addStatusCell(table, nvl(p.getPanStatus()), bg);
            addTd(table, formatDate(p.getVerifiedAt()), bg, Element.ALIGN_LEFT);
            idx++;
        }

        doc.add(table);
    }

    private void addTh(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TH));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(8);
        cell.setBorderColor(new BaseColor(30, 64, 175));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTd(PdfPTable table, String text, BaseColor bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TD));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addStatusCell(PdfPTable table, String status, BaseColor bg) {
        BaseColor badgeColor;
        switch (status.toUpperCase()) {
            case "VALID":    badgeColor = SUCCESS; break;
            case "INVALID":  badgeColor = DANGER;  break;
            default:         badgeColor = WARN;    break;
        }

        PdfPCell outer = new PdfPCell();
        outer.setBackgroundColor(bg);
        outer.setPadding(5);
        outer.setBorderColor(BORDER_COLOR);
        outer.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPTable badge = new PdfPTable(1);
        badge.setWidthPercentage(70);
        PdfPCell inner = new PdfPCell(new Phrase(status.toUpperCase(), FONT_BADGE));
        inner.setBackgroundColor(badgeColor);
        inner.setPadding(4);
        inner.setBorder(Rectangle.NO_BORDER);
        inner.setHorizontalAlignment(Element.ALIGN_CENTER);
        badge.addCell(inner);

        outer.addElement(badge);
        table.addCell(outer);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Page event: header stripe + footer
    // ─────────────────────────────────────────────────────────────────────────
    static class HeaderFooterEvent extends PdfPageEventHelper {
        private final String role;
        private final String username;

        HeaderFooterEvent(String role, String username) {
            this.role     = role;
            this.username = username;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle page   = document.getPageSize();

            // Top accent line
            cb.setColorFill(new BaseColor(30, 64, 175));
            cb.rectangle(0, page.getTop() - 5, page.getWidth(), 5);
            cb.fill();

            // Footer line
            cb.setLineWidth(0.5f);
            cb.setColorStroke(new BaseColor(203, 213, 225));
            cb.moveTo(40, 42);
            cb.lineTo(page.getWidth() - 40, 42);
            cb.stroke();

            // Footer text
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 7, new BaseColor(100, 116, 139));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("PAN Verification System  |  Role: " + role
                            + "  |  User: " + username, f),
                    40, 30, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), f),
                    page.getWidth() - 40, 30, 0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────
    private String nvl(String s)                    { return s != null ? s : "-"; }
    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}