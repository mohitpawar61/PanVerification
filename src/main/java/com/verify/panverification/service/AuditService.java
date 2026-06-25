package com.verify.panverification.service;

import com.verify.panverification.entity.AuditLog;
import com.verify.panverification.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void saveLog(
            String action,
            String username
    ){

        log.info("Audit Log Saved | User={} Action={}",
                username, action);

        AuditLog log =
                AuditLog.builder()
                        .action(action)
                        .username(username)
                        .timestamp(LocalDateTime.now())
                        .build();

        auditLogRepository.save(log);
    }
}
