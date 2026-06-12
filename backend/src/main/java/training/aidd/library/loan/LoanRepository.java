package training.aidd.library.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    long countByMemberIdAndReturnedDateIsNull(Long memberId);
}
