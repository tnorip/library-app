package training.aidd.library.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CopyCheckedOutException extends RuntimeException {
    public CopyCheckedOutException(Long copyId) {
        super("貸出中のため現物を削除できません: copyId=" + copyId);
    }
}
