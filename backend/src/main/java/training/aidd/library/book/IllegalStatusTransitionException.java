package training.aidd.library.book;

public class IllegalStatusTransitionException extends RuntimeException {
    public IllegalStatusTransitionException(CopyStatus from, CopyStatus to) {
        super("不正なステータス遷移: " + from + " → " + to);
    }
}
