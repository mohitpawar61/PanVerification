package com.verify.panverification.controller;

import com.verify.panverification.dto.DashboardResponse;
import com.verify.panverification.repository.PanVerificationRepository;
import com.verify.panverification.repository.UserRepository;
import com.verify.panverification.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final PanVerificationRepository panVerificationRepository;


    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {

        log.info("Dashboard API called");

        DashboardResponse response = dashboardService.dashboard();

        log.info("Dashboard data fetched successfully");

        return response;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {

        log.info("Stats API called");

        long totalUsers = userRepository.count();
        long totalVerifications = panVerificationRepository.count();

        log.info("Stats fetched. Users={} Verifications={}",
                totalUsers, totalVerifications);

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalVerifications", totalVerifications);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", data
                )
        );
    }


}