package training.aidd.library.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequest(
        @NotNull @Positive Long bookId,
        @NotNull @Positive Long memberId
) {}
