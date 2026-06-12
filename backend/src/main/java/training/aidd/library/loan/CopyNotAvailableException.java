package training.aidd.library.loan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CopyNotAvailableException extends RuntimeException {
    public CopyNotAvailableException(Long copyId) {
        super("現物が貸出可能ではありません: copyId=" + copyId);
    }
}
