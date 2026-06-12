package training.aidd.library.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyReservedException extends RuntimeException {
    public AlreadyReservedException(Long bookId, Long memberId) {
        super("Member " + memberId + " already has an active reservation for book " + bookId);
    }
}
