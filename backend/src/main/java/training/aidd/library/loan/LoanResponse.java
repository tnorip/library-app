package training.aidd.library.loan;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long memberId,
        String memberName,
        Long bookCopyId,
        String copyCode,
        String bookTitle,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnedDate
) {}
