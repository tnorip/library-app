package training.aidd.library.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String targetType,
        String targetId,
        String performedBy,
        LocalDateTime performedAt
) {}
