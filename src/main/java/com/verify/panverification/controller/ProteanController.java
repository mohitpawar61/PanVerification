package com.verify.panverification.controller;

import com.verify.panverification.dto.PanRequest;
import com.verify.panverification.service.ProteanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/protean")
public class ProteanController {

    private final ProteanService proteanService;

    @PostMapping("/proteanverify")
    public String verifyPan(@RequestBody PanRequest panRequest)  {

        log.info("Protean Verify API called for PAN: {}",panRequest.pan());

        String response = proteanService.verifyPan(panRequest);

        log.info("Protean Verify API completed for PAN: {}",panRequest.pan());

        return response;
    }
}
