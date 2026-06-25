package com.verify.panverification.controller;

import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource>
    downloadPdf() throws Exception {

        log.info("PDF Report Download Requested");

        ByteArrayInputStream pdf =
                reportService.generatePdf();

        log.info("PDF Report Generated Successfully");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pan-report.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(
                        new InputStreamResource(pdf)
                );
    }

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource>
    downloadExcel() throws Exception {

        log.info("Excel Report Download Requested");
        ByteArrayInputStream excel =
                reportService.generateExcel();
        log.info("Excel Report Generated Successfully");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pan-report.xlsx"
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(
                        new InputStreamResource(excel)
                );
    }
}