package training.aidd.library.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByPerformedAtDesc();
    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(String performedBy);
}
