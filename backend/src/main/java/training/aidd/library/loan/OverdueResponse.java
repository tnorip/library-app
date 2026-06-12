package training.aidd.library.loan;

import java.time.LocalDate;

public record OverdueResponse(
        Long loanId,
        Long memberId,
        String memberName,
        String memberEmail,
        String bookTitle,
        String copyCode,
        LocalDate dueDate,
        long overdueDays
) {}
