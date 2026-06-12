package training.aidd.library.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookHasCopiesException extends RuntimeException {
    public BookHasCopiesException(Long bookId) {
        super("現物が登録されているため書誌を削除できません: bookId=" + bookId);
    }
}
