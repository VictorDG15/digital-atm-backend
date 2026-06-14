package com.portfolio.digitalatm.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, String action, String result, String ip) {
        AuditLog logEntry = AuditLog.builder()
                .username(username == null ? "anonymous" : username)
                .action(action)
                .result(result)
                .ip(ip)
                .build();
        auditLogRepository.save(logEntry);
        log.info("audit action={} user={} result={}", action, logEntry.getUsername(), result);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(log -> new AuditLogResponse(log.getId(), log.getUsername(), log.getAction(), log.getResult(), log.getIp(), log.getCreatedAt()))
                .toList();
    }
}
