package com.verify.panverification.service;

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

    public String verifyPan(PanRequest panRequest) {

        log.info("Protean PAN verification started for PAN: {}",panRequest.pan());
        try {

            List<PanRequest> inputData =
                    List.of(panRequest);


            String inputDataJson =
                    objectMapper.writeValueAsString(inputData);

            log.debug("Input Data JSON: {}", inputDataJson);

            String signature =
                    signatureService.generateSignature(
                            inputDataJson
                    );
            log.debug("Signature generated successfully");




            OpvRequest request =
                    new OpvRequest(
                            userId,
                            String.valueOf(inputData.size()),
                            LocalDateTime.now()
                                    .format(
                                            DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd'T'HH:mm:ss"
                                            )
                                    ),
                            userId + ":" + System.currentTimeMillis(),
                            version,
                            inputData,
                            signature
                    );


            String requestJson =
                    objectMapper.writeValueAsString(
                            request
                    );
            log.debug("Final Request JSON: {}", requestJson);




            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            HttpEntity<String> entity =
                    new HttpEntity<>(
                            requestJson,
                            headers
                    );


            log.info("Calling Protean API at URL: {}", url);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );

            log.info("Protean API Response Status: {}", response.getStatusCode());
            log.debug("Protean API Response Body: {}", response.getBody());

            return response.getBody();

        } catch (Exception e) {

            log.error("Protean PAN verification failed: {}", e.getMessage(), e);

            return "ERROR : "
                    + e.getMessage();
        }
    }
}