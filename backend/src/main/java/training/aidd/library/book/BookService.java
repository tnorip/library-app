package training.aidd.library.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.aidd.library.audit.Audit;
import training.aidd.library.loan.LoanRepository;

import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository,
                       BookCopyRepository bookCopyRepository,
                       LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll(String q, String callNumber) {
        List<Book> books;
        if (q != null && !q.isBlank()) {
            books = bookRepository.findByTitleContainingOrAuthorContaining(q, q);
        } else if (callNumber != null && !callNumber.isBlank()) {
            books = bookRepository.findByCallNumberStartingWith(callNumber);
        } else {
            books = bookRepository.findAll();
        }
        return books.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return toResponse(book);
    }

    @Audit(action = "BOOK_CREATED", targetType = "Book")
    public BookResponse create(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        Book savedBook = bookRepository.save(book);
        return toResponse(savedBook);
    }

    @Audit(action = "BOOK_UPDATED", targetType = "Book")
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        applyRequest(book, request);
        Book savedBook = bookRepository.save(book);
        return toResponse(savedBook);
    }

    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        validateNoCopiesExist(id);
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<BookCopyResponse> findCopies(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return bookCopyRepository.findByBookId(bookId).stream()
                .map(this::toCopyResponse)
                .toList();
    }

    public BookCopyResponse addCopy(Long bookId, BookCopyRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setCopyCode(request.copyCode());
        copy.setStatus(CopyStatus.AVAILABLE);
        return toCopyResponse(bookCopyRepository.save(copy));
    }

    public BookCopyResponse updateCopyStatus(Long bookId, Long copyId, CopyStatus newStatus) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new BookNotFoundException(copyId));
        validateCopyNotCheckedOut(copy, newStatus);
        copy.setStatus(newStatus);
        return toCopyResponse(bookCopyRepository.save(copy));
    }

    public void deleteCopy(Long bookId, Long copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new BookNotFoundException(copyId));
        if (copy.getStatus() == CopyStatus.CHECKED_OUT) {
            throw new CopyCheckedOutException(copyId);
        }
        bookCopyRepository.delete(copy);
    }

    // ─── Business Logic ────────────────────────────────────────────────────────

    private void validateNoCopiesExist(Long bookId) {
        if (!bookCopyRepository.findByBookId(bookId).isEmpty()) {
            throw new BookHasCopiesException(bookId);
        }
    }

    private void validateCopyNotCheckedOut(BookCopy copy, CopyStatus newStatus) {
        if (copy.getStatus() == CopyStatus.CHECKED_OUT && newStatus == CopyStatus.AVAILABLE) {
            throw new IllegalStatusTransitionException(CopyStatus.CHECKED_OUT, CopyStatus.AVAILABLE);
        }
    }

    // ─── Mapping ───────────────────────────────────────────────────────────────

    private BookResponse toResponse(Book book) {
        List<BookCopyResponse> copies = book.getCopies().stream()
                .map(this::toCopyResponse)
                .toList();
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishedYear(),
                book.getCallNumber(),
                copies
        );
    }

    private BookCopyResponse toCopyResponse(BookCopy copy) {
        var dueDate = copy.getStatus() == CopyStatus.CHECKED_OUT
                ? loanRepository.findFirstByBookCopyIdAndReturnedDateIsNull(copy.getId())
                        .map(loan -> loan.getDueDate())
                        .orElse(null)
                : null;
        return new BookCopyResponse(copy.getId(), copy.getCopyCode(), copy.getStatus(), dueDate);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setPublishedYear(request.publishedYear());
        book.setCallNumber(request.callNumber());
    }
}
