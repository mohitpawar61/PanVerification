package com.verify.panverification.controller;

import com.verify.panverification.service.ProteanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/protean")
@CrossOrigin("*")
public class ProteanController {

    private final ProteanService proteanService;

    @GetMapping("/proteanverify")
    public String verifyPan() throws Exception {

        log.info("Protean Verification API called");

        String response = proteanService.verifyPan();

        log.info("Protean Verification completed");

        return response;
    }
}
