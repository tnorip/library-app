package training.aidd.library.reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberName,
        LocalDateTime reservedAt,
        ReservationStatus status
) {}
