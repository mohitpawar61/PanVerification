package com.verify.panverification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify.panverification.dto.PanRequest;
import com.verify.panverification.dto.PanVerificationRequest;
import com.verify.panverification.dto.PanVerificationResponse;
import com.verify.panverification.entity.*;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.repository.ProteanResponseHeaderRepository;
import com.verify.panverification.util.ExcelGenerator;
import com.verify.panverification.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PanVerificationService {

    private final PanVerificationRepository panVerify;
    private final ProteanResponseHeaderRepository headerRepository;
    private final ProteanService proteanService;
    private final ObjectMapper objectMapper;
    private final PdfGenerator pdfGenerator;
    private final ExcelGenerator excelGenerator;

    private static final DateTimeFormatter PROTEAN_DOB_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PanVerificationResponse verify(PanVerificationRequest request,User currentUser) throws Exception {

        log.info("PAN Verification Started for PAN={}", request.panNumber());

        String dobForProtean = request.dob().format(PROTEAN_DOB_FORMAT);
        log.debug("DOB formatted for Protean: {}", dobForProtean);

        PanRequest panRequest = new PanRequest(
                request.panNumber(),
                request.fullName(),
                request.fathername(),
                dobForProtean
        );

        //Call Protean API
        ResponseEntity<String> rawResponse = proteanService.verifyPan(panRequest);
        String responseBody   = rawResponse.getBody();
        HttpHeaders respHeaders = rawResponse.getHeaders();

        String panStatus          = null;
        String verificationStatus = "FAILED";
        String responseCode       = null;

        //  Save pan_verification
        PanVerification verification = new PanVerification();
        verification.setUser(currentUser);
        verification.setVerifiedAt(java.time.LocalDateTime.now());
        verification.setPanNumber(request.panNumber());
        verification.setFullName(request.fullName());
        verification.setFathername(request.fathername());
        verification.setDob(request.dob());

        //Build protean_response_header
        ProteanResponseHeader header = new ProteanResponseHeader();
        header.setUserId(respHeaders.getFirst("User_ID"));
        header.setRecordsCount(respHeaders.getFirst("Records_count"));
        header.setResponseTime(respHeaders.getFirst("Response_time"));
        header.setTransactionId(respHeaders.getFirst("Transaction_ID"));
        header.setVersion(respHeaders.getFirst("Version"));

        List<ProteanOutputData> outputDataList = new ArrayList<>();

        // Parse response body
        if (responseBody != null && !responseBody.isBlank()
                && !responseBody.startsWith("ERROR")) {

            JsonNode root = objectMapper.readTree(responseBody);
            responseCode  = root.path("response_Code").asText(null);
            header.setResponseCode(responseCode);

            if ("1".equals(responseCode)) {
                JsonNode outputData = root.path("outputData");
                if (outputData.isArray() && !outputData.isEmpty()) {
                    panStatus          = outputData.get(0).path("pan_status").asText(null);
                    verificationStatus = "SUCCESS";

                    //  Build protean_output_data rows
                    for (JsonNode item : outputData) {
                        ProteanOutputData od = new ProteanOutputData();
                        od.setPan(item.path("pan").asText(null));
                        od.setPanStatus(item.path("pan_status").asText(null));
                        od.setName(item.path("name").asText(null));
                        od.setFathername(item.path("fathername").asText(null));
                        od.setDob(item.path("dob").asText(null));
                        od.setSeedingStatus(item.path("seeding_status").asText(null));
                        od.setHeader(header);
                        outputDataList.add(od);
                    }
                }
            } else {
                log.warn("Protean Error [{}] : {}", responseCode,
                        getResponseMessage(responseCode));
            }

        } else {
            log.error("Protean call returned error: {}", responseBody);
            header.setResponseCode("ERROR");
        }

        //Step 5: Save all to DB
        verification.setPanStatus(panStatus);
        verification.setVerificationStatus(verificationStatus);
        panVerify.save(verification);

        header.setPanVerification(verification);
        header.setOutputData(outputDataList);
        headerRepository.save(header);


        log.info("PAN Verification Saved | PAN={} | Status={}",
                verification.getPanNumber(), verification.getVerificationStatus());

        return new PanVerificationResponse(
                verification.getPanNumber(),
                verification.getPanStatus(),
                verification.getVerificationStatus(),
                getResponseMessage(responseCode)
        );
    }

    public ByteArrayInputStream exportPdf(User currentUser) throws Exception {
        log.info("Export PDF started for user: {}", currentUser.getEmail());

        List<PanVerification> records = currentUser.getRole() == Role.ADMIN
                ? panVerify.findAllByOrderByIdDesc()
                : panVerify.findByUserOrderByIdDesc(currentUser);

        log.info("Records found: {}", records.size());

        ByteArrayInputStream stream =
                pdfGenerator.generate(
                        records,
                        currentUser.getRole().name(),
                        currentUser.getFullName());

        log.info("PDF generated successfully");

        return stream;
    }

    public ByteArrayInputStream exportExcel(User currentUser) throws Exception {
        log.info("Export Excel started for user: {}", currentUser.getEmail());

        List<PanVerification> records = currentUser.getRole() == Role.ADMIN
                ? panVerify.findAllByOrderByIdDesc()
                : panVerify.findByUserOrderByIdDesc(currentUser);



        ByteArrayInputStream stream =
                excelGenerator.generate(
                        records,
                        currentUser.getRole().name(),
                        currentUser.getFullName());

        log.info("Excel generated successfully");

        return stream;
    }

    private String getResponseMessage(String responseCode) {
        if (responseCode == null) return "Unknown Error";
        return switch (responseCode) {
            case "1"  -> "Success";
            case "2"  -> "System Error";
            case "3"  -> "Authentication Failure - Check certificate or User ID";
            case "4"  -> "User not authorized";
            case "5"  -> "No PANs entered or limit exceeded";
            case "6"  -> "User validity expired";
            case "8"  -> "Not enough balance";
            case "12" -> "Invalid version number";
            case "18" -> "Wrong User ID or Certificate";
            case "19" -> "Digital signature missing";
            case "20" -> "Request body is blank";
            case "22" -> "Invalid PAN format";
            case "23" -> "System Failure";
            case "24" -> "Duplicate Transaction ID";
            case "25" -> "JSON Parse Exception";
            case "26" -> "Records Count mismatch";
            case "27" -> "Invalid Name";
            case "28" -> "Invalid Father Name";
            case "29" -> "Invalid DOB format";
            case "30" -> "Invalid Request Time";
            case "31" -> "Invalid Transaction ID";
            case "32" -> "Invalid Record Count";
            case "33" -> "Request Time not within last 30 minutes";
            default   -> "Unknown Error (code=" + responseCode + ")";
        };
    }

    public List<PanVerification> search(String pan, User currentUser) {

        log.info("Searching PAN Records for {}", pan);

        if (currentUser.getRole() == Role.ADMIN) {
            return panVerify.findByPanNumberContaining(pan,currentUser);
        }

        return panVerify.findByUserOrderByIdDesc(currentUser)
                .stream()
                .filter(v -> v.getPanNumber().contains(pan))
                .toList();
    }

    public List<PanVerification> getHistory(User currentUser) {

        log.info("Fetching PAN Verification History");

        if (currentUser.getRole() == Role.ADMIN) {
            return panVerify.findAllByOrderByIdDesc();
        }

        return panVerify.findByUserOrderByIdDesc(currentUser);
    }

}