package training.aidd.library.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import training.aidd.library.loan.AlreadyReturnedException;
import training.aidd.library.loan.CopyNotAvailableException;
import training.aidd.library.loan.LoanLimitExceededException;
import training.aidd.library.loan.LoanNotFoundException;
import training.aidd.library.loan.MemberNotActiveException;
import training.aidd.library.member.MemberNotFoundException;
import training.aidd.library.reservation.AlreadyReservedException;
import training.aidd.library.reservation.ReservationNotFoundException;
import training.aidd.library.staff.StaffNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBookNotFound(BookNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(BookHasCopiesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookHasCopies(BookHasCopiesException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse(400, ex.getMessage());
    }

    @ExceptionHandler(CopyCheckedOutException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCopyCheckedOut(CopyCheckedOutException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(409, ex.getMessage());
    }

    @ExceptionHandler(IllegalStatusTransitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalStatusTransition(IllegalStatusTransitionException ex) {
        log.warn("Illegal status transition: {}", ex.getMessage());
        return new ErrorResponse(400, ex.getMessage());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMemberNotFound(MemberNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(LoanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleLoanNotFound(LoanNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(CopyNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCopyNotAvailable(CopyNotAvailableException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(409, ex.getMessage());
    }

    @ExceptionHandler(LoanLimitExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleLoanLimit(LoanLimitExceededException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse(400, ex.getMessage());
    }

    @ExceptionHandler(MemberNotActiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMemberNotActive(MemberNotActiveException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse(400, ex.getMessage());
    }

    @ExceptionHandler(StaffNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleStaffNotFound(StaffNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReservationNotFound(ReservationNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(AlreadyReservedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyReserved(AlreadyReservedException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(409, ex.getMessage());
    }

    @ExceptionHandler(AlreadyReturnedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyReturned(AlreadyReturnedException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return new ErrorResponse(409, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex) {
        // スタックトレースはログにのみ残し、クライアントには漏らさない
        log.error("Unexpected error", ex);
        return new ErrorResponse(500, "予期しないエラーが発生しました");
    }
}
