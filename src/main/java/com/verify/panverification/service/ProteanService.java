package com.verify.panverification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify.panverification.dto.OpvRequest;
import com.verify.panverification.dto.PanRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProteanService {

    private final SignatureService signatureService;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;


    @Value("${protean.user-id}")
    private String userId;

    @Value("${protean.version}")
    private String version;

    @Value("${protean.uat-url}")
    private String url;

    public ResponseEntity<String> verifyPan(PanRequest panRequest) {

        log.info("Protean PAN verification started for PAN: {}", panRequest.pan());
        try {

            List<PanRequest> inputData = List.of(panRequest);

            String inputDataJson = objectMapper.writeValueAsString(inputData);
            log.debug("Input Data JSON: {}", inputDataJson);

            String signature = signatureService.generateSignature(inputDataJson);
            log.debug("Signature generated successfully");

            String recordsCount  = String.valueOf(inputData.size());
            String requestTime   = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            String transactionId = userId + ":" + System.currentTimeMillis();

            OpvRequest request = new OpvRequest(
                    userId,
                    recordsCount,
                    requestTime,
                    transactionId,
                    version,
                    inputData,
                    signature
            );

            log.info("---------------------------------------------------------------");
            log.info("PROTEAN API - REQUEST HEADERS");
            log.info("  User_ID        : {}", request.User_ID());
            log.info("  Records_count  : {}", request.Records_count());
            log.info("  Request_time   : {}", request.Request_time());
            log.info("  Transaction_ID : {}", request.Transaction_ID());
            log.info("  Version        : {}", request.Version());
            log.info("---------------------------------------------------------------");

            String requestJson = objectMapper.writeValueAsString(request);
            log.debug("Final Request JSON: {}", requestJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User_ID",        userId);
            headers.set("Records_count",  recordsCount);
            headers.set("Request_time",   requestTime);
            headers.set("Transaction_ID", transactionId);
            headers.set("Version",        version);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            log.info("Calling Protean API at URL: {}", url);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            log.info("Protean API Response Status: {}", response.getStatusCode());

            HttpHeaders respHeaders = response.getHeaders();
            log.info("---------------------------------------------------------------");
            log.info("PROTEAN API - RESPONSE HEADERS");
            log.info("  User_ID        : {}", firstHeader(respHeaders, "User_ID"));
            log.info("  Records_count  : {}", firstHeader(respHeaders, "Records_count"));
            log.info("  Response_time  : {}", firstHeader(respHeaders, "Response_time"));
            log.info("  Transaction_ID : {}", firstHeader(respHeaders, "Transaction_ID"));
            log.info("  Version        : {}", firstHeader(respHeaders, "Version"));
            log.info("---------------------------------------------------------------");

            String responseBody = response.getBody();
            log.debug("Protean API Response Body: {}", responseBody);

            if (responseBody != null && !responseBody.isBlank()) {
                try {
                    JsonNode root = objectMapper.readTree(responseBody);

                    log.info("PROTEAN API - RESPONSE BODY");
                    log.info("  response_Code  : {}", root.path("response_Code").asText("missing"));

                    JsonNode outputData = root.path("outputData");
                    if (outputData.isArray() && !outputData.isEmpty()) {
                        log.info("  outputData     : {} record(s)", outputData.size());
                        for (int i = 0; i < outputData.size(); i++) {
                            JsonNode item = outputData.get(i);
                            log.info("  outputData[{}] :", i);
                            log.info("    pan            : {}", item.path("pan").asText(""));
                            log.info("    pan_status     : {}", item.path("pan_status").asText(""));
                            log.info("    name           : {}", item.path("name").asText(""));
                            log.info("    fathername     : {}", item.path("fathername").asText(""));
                            log.info("    dob            : {}", item.path("dob").asText(""));
                            log.info("    seeding_status : {}", item.path("seeding_status").asText(""));
                        }
                    } else {
                        log.info("  outputData     : [] (empty)");
                    }
                    log.info("---------------------------------------------------------------");

                } catch (Exception ex) {
                    log.warn("Could not parse response body as JSON: {}", ex.getMessage());
                }
            }

            return response;

        } catch (Exception e) {
            log.error("Protean PAN verification failed: {}", e.getMessage(), e);
            throw new RuntimeException("Protean API call failed: " + e.getMessage(), e);
        }
    }

    private String firstHeader(HttpHeaders headers, String name) {
        String val = headers.getFirst(name);
        return val != null ? val : "(not present)";
    }
}