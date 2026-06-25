package com.verify.panverification.service;

import com.verify.panverification.dto.DashboardResponse;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final PanVerificationRepository panVerificationRepository;

    public DashboardResponse dashboard() {

        log.info("Fetching dashboard statistics");
        long users =
                userRepository.count();

        long verifications =
                panVerificationRepository.count();

        log.info("Dashboard Stats -> Users={} Verifications={}",
                users, verifications);

        return new DashboardResponse(
                users,
                verifications
        );
    }
}
