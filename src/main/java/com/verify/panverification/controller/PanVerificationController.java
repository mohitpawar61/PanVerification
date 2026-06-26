package com.verify.panverification.controller;

import com.verify.panverification.dto.ApiResponse;
import com.verify.panverification.dto.PanVerificationRequest;
import com.verify.panverification.dto.PanVerificationResponse;
import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.service.PanVerificationService;
import com.verify.panverification.service.SignatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pan")
@RequiredArgsConstructor
public class PanVerificationController {

    private final PanVerificationService panVerificationService;
    private final SignatureService signatureService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(
           @Valid @RequestBody PanVerificationRequest request) throws Exception {

        log.info("PAN Verification Request Received for PAN={}",
                request.panNumber());
        PanVerificationResponse response =
                panVerificationService.verify(request);

        log.info("PAN Verification Completed for PAN={}",
                request.panNumber());

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "PAN Verified",
                        response
                )
        );
    }

    @GetMapping("/history")
    public List<PanVerification> history()
    {

        log.info("History API called");
        return panVerificationService
                .getHistory();
    }

    @GetMapping("/search")
    public List<PanVerification> search(
            @RequestParam String pan){

        log.info("Search API called for PAN={}", pan);
        return panVerificationService
                .search(pan);
    }

    @GetMapping("/test-cert")
    public String testCertificate() {

        log.info("Certificate Test API called");
        return signatureService.testCertificate();
    }

    @PostMapping("/generate-signature")
    public String generateSignature(
            @RequestBody String json)
            throws Exception {

        log.info("Generate Signature API called");
        return signatureService
                .generateSignature(json);
    }

    @GetMapping("/test-sign")
    public String testSign() {

        return signatureService.testSignature();
    }
}
