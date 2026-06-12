package training.aidd.library.loan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyReturnedException extends RuntimeException {
    public AlreadyReturnedException(Long loanId) {
        super("すでに返却済みです: loanId=" + loanId);
    }
}
