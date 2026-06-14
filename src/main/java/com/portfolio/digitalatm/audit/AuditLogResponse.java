package com.portfolio.digitalatm.audit;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String username,
        String action,
        String result,
        String ip,
        Instant createdAt
) {
}
