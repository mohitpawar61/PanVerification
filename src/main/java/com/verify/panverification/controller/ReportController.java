package com.verify.panverification.controller;

import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.entity.User;
import com.verify.panverification.service.PanVerificationService;
import com.verify.panverification.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PanVerificationService panVerificationService;

    @GetMapping("/pdf")
    public ResponseEntity<Resource> exportPdf(@AuthenticationPrincipal User currentUser)
            throws Exception {

        log.info("PDF export request received from user: {}",
                currentUser.getEmail());

        ByteArrayInputStream stream = panVerificationService.exportPdf(currentUser);

        log.info("PDF report generated successfully for user: {}",
                currentUser.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pan-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/excel")
    public ResponseEntity<Resource> exportExcel(@AuthenticationPrincipal User currentUser)
            throws Exception {

        log.info("Excel export request received from user: {}",
                currentUser.getEmail());
        ByteArrayInputStream stream = panVerificationService.exportExcel(currentUser);

        log.info("Excel report generated successfully for user: {}",
                currentUser.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pan-report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}