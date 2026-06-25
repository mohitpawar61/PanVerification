package com.verify.panverification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verify.panverification.dto.OpvRequest;
import com.verify.panverification.dto.PanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public String verifyPan() {

        try {


            PanRequest panRequest =
                    new PanRequest(
                            "ABCDE1234F",
                            "TEST USER",
                            "",
                            "01/01/1990"
                    );

            List<PanRequest> inputData =
                    List.of(panRequest);


            String inputDataJson =
                    objectMapper.writeValueAsString(inputData);

            System.out.println("Input Data JSON:");
            System.out.println(inputDataJson);

            String signature =
                    signatureService.generateSignature(
                            inputDataJson
                    );

            System.out.println("Generated Signature:");
            System.out.println(signature);


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

            System.out.println("Final Request JSON:");
            System.out.println(requestJson);


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


            System.out.println(
                    "Calling Protean API..."
            );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );

            System.out.println(
                    "Response Status : "
                            + response.getStatusCode()
            );

            System.out.println(
                    "Response Body : "
                            + response.getBody()
            );

            return response.getBody();

        } catch (Exception e) {

            e.printStackTrace();

            return "ERROR : "
                    + e.getMessage();
        }
    }
}