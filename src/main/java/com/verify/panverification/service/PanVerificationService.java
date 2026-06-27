package com.verify.panverification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify.panverification.dto.PanRequest;
import com.verify.panverification.dto.PanVerificationRequest;
import com.verify.panverification.dto.PanVerificationResponse;
import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PanVerificationService {

    private final PanVerificationRepository panVerify;
    private final ProteanService proteanService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter PROTEAN_DOB_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");



    public PanVerificationResponse verify(
            PanVerificationRequest request
    ) throws Exception{

        log.info("PAN Verification Started for PAN={}",
                request.panNumber());

        String dobForProtean = request.dob().format(PROTEAN_DOB_FORMAT);
        log.debug("DOB formatted for Protean: {}",dobForProtean);

        PanRequest panRequest = new PanRequest(

                request.panNumber(),
                request.fullName(),
                request.fathername(),
                dobForProtean
        );

        ResponseEntity<String> rawResponse = proteanService.verifyPan(panRequest);
        String responseBody = rawResponse.getBody();



        String panStatus       = null;
        String verificationStatus = "FAILED";


        if (responseBody != null && !responseBody.isBlank() && !responseBody.startsWith("ERROR")) {

            JsonNode root = objectMapper.readTree(responseBody);
            String responseCode  = root.path("response_Code").asText(null);

            if ("1".equals(responseCode)) {
                JsonNode outputData = root.path("outputData");
                if (outputData.isArray() && !outputData.isEmpty()) {
                    JsonNode first = outputData.get(0);
                    panStatus         = first.path("pan_status").asText(null);
                    verificationStatus = "SUCCESS";
                }
            } else {
                log.warn("Protean returned non-success response_Code={}", responseCode);
            }
        }else {
            log.error("Protean call returned error: {}",responseBody);
        }

        // Save to DB
        PanVerification verification = new PanVerification();
        verification.setPanNumber(request.panNumber());
        verification.setFullName(request.fullName());
        verification.setFathername(request.fathername());
        verification.setDob(request.dob());
        verification.setPanStatus(panStatus);
        verification.setVerificationStatus(verificationStatus);


        panVerify.save(verification);

        log.info("PAN Verification Saved Successfully | PAN={} | Status={}",
                verification.getPanNumber(),verification.getVerificationStatus());

        return new PanVerificationResponse(
                verification.getPanNumber(),
                verification.getPanStatus(),
                verification.getVerificationStatus(),
                "PAN Verification" + verificationStatus
        );
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

    public List<PanVerification>
    search(String pan){
        log.info("Searching PAN Records for {}", pan);
        return panVerify
                .findByPanNumberContaining(pan);
    }

    public List<PanVerification> getHistory() {
        log.info("Fetching PAN Verification History");
        return panVerify
                .findAllByOrderByIdDesc();
    }
}
