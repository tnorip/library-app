package training.aidd.library.loan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MemberNotActiveException extends RuntimeException {
    public MemberNotActiveException(Long memberId) {
        super("退会済みの利用者には貸出できません: memberId=" + memberId);
    }
}
