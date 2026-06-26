package com.verify.panverification.service;

import com.verify.panverification.dto.PanVerificationRequest;
import com.verify.panverification.dto.PanVerificationResponse;
import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PanVerificationService {

    private final PanVerificationRepository panVerify;



    public PanVerificationResponse verify(
            PanVerificationRequest request
    ) throws Exception{

        log.info("PAN Verification Started for PAN={}",
                request.panNumber());



        PanVerification verification =
                new PanVerification();

        verification.setPanNumber(
                request.panNumber()
        );

        verification.setFullName(
                request.fullName()
        );

        verification.setFathername(
                request.fathername()
        );

        verification.setDob(
                request.dob()
        );

        verification.setPanStatus("E");



        panVerify.save(verification);

        log.info(
                "PAN Verification Saved Successfully | PAN={} | Status={}",
                verification.getPanNumber(),
                verification.getVerificationStatus()
        );

        return new PanVerificationResponse(
                verification.getPanNumber(),
                verification.getPanStatus(),
                verification.getVerificationStatus(),
                "PAN Verification Successful"
        );
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
