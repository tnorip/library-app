package training.aidd.library.loan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.audit.AuditLogService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OverdueService {

    private static final Logger log = LoggerFactory.getLogger(OverdueService.class);

    private final LoanRepository loanRepository;
    private final AuditLogService auditLogService;

    public OverdueService(LoanRepository loanRepository, AuditLogService auditLogService) {
        this.loanRepository = loanRepository;
        this.auditLogService = auditLogService;
    }

    public List<OverdueResponse> findOverdue() {
        return loanRepository.findByReturnedDateIsNullAndDueDateBefore(LocalDate.now())
                .stream()
                .map(this::toOverdueResponse)
                .toList();
    }

    @Transactional
    public List<OverdueResponse> sendReminders(String operator) {
        List<OverdueResponse> overdue = findOverdue();

        overdue.forEach(o -> {
            // 実メール送信はここに差し込む (JavaMailSender 等)
            log.info("[督促通知] {} <{}> 「{}」{} 日延滞",
                    o.memberName(), o.memberEmail(), o.bookTitle(), o.overdueDays());

            auditLogService.log(
                    "OVERDUE_NOTIFIED",
                    "Loan",
                    String.valueOf(o.loanId()),
                    operator
            );
        });

        return overdue;
    }

    private OverdueResponse toOverdueResponse(Loan loan) {
        long days = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        return new OverdueResponse(
                loan.getId(),
                loan.getMember().getId(),
                loan.getMember().getName(),
                loan.getMember().getEmail(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getCopyCode(),
                loan.getDueDate(),
                days
        );
    }
}
