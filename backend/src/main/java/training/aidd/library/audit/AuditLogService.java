package training.aidd.library.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // REQUIRES_NEW: 呼び出し元のトランザクションとは独立して保存する
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String targetType, String targetId, String performedBy) {
        auditLogRepository.save(new AuditLog(action, targetType, targetId, performedBy));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findRecent() {
        return auditLogRepository.findTop100ByOrderByPerformedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByUser(String performedBy) {
        return auditLogRepository.findByPerformedByOrderByPerformedAtDesc(performedBy)
                .stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getAction(), log.getTargetType(),
                log.getTargetId(), log.getPerformedBy(), log.getPerformedAt()
        );
    }
}
