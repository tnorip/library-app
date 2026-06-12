package training.aidd.library.loan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_rule")
public class LoanRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxLoanCount = 10;
    private Integer loanPeriodDays = 14;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getMaxLoanCount() { return maxLoanCount; }
    public void setMaxLoanCount(Integer maxLoanCount) { this.maxLoanCount = maxLoanCount; }

    public Integer getLoanPeriodDays() { return loanPeriodDays; }
    public void setLoanPeriodDays(Integer loanPeriodDays) { this.loanPeriodDays = loanPeriodDays; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
