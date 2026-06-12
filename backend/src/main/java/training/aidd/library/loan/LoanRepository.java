package training.aidd.library.loan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    long countByMemberIdAndReturnedDateIsNull(Long memberId);

    Optional<Loan> findFirstByBookCopyIdAndReturnedDateIsNull(Long bookCopyId);

    long countByReturnedDateIsNull();

    long countByReturnedDateIsNullAndDueDateBefore(LocalDate date);

    List<Loan> findByReturnedDateIsNullAndDueDateBefore(LocalDate date);

    List<Loan> findByLoanDateAfter(LocalDate date);
}
