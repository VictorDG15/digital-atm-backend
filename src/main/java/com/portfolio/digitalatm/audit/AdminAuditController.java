package com.portfolio.digitalatm.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {
    private final AuditService auditService;

    @GetMapping
    public List<AuditLogResponse> findAll() {
        return auditService.findAll();
    }
}
