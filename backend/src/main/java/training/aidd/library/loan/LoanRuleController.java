package training.aidd.library.loan;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/loan-rules")
public class LoanRuleController {

    private final LoanRuleRepository loanRuleRepository;

    public LoanRuleController(LoanRuleRepository loanRuleRepository) {
        this.loanRuleRepository = loanRuleRepository;
    }

    @GetMapping
    public LoanRuleResponse getRule() {
        LoanRule rule = loanRuleRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> loanRuleRepository.save(new LoanRule()));
        return toResponse(rule);
    }

    @PutMapping
    public LoanRuleResponse updateRule(@RequestBody LoanRuleRequest request) {
        LoanRule rule = loanRuleRepository.findAll().stream()
                .findFirst()
                .orElseGet(LoanRule::new);
        if (request.maxLoanCount() != null) rule.setMaxLoanCount(request.maxLoanCount());
        if (request.loanPeriodDays() != null) rule.setLoanPeriodDays(request.loanPeriodDays());
        rule.setUpdatedAt(LocalDateTime.now());
        return toResponse(loanRuleRepository.save(rule));
    }

    private LoanRuleResponse toResponse(LoanRule rule) {
        return new LoanRuleResponse(rule.getId(), rule.getMaxLoanCount(), rule.getLoanPeriodDays());
    }
}
