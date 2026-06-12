package training.aidd.library.loan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LoanLimitExceededException extends RuntimeException {
    public LoanLimitExceededException(Long memberId, int limit) {
        super("貸出上限（" + limit + "冊）に達しています: memberId=" + memberId);
    }
}
