package training.aidd.library.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.aidd.library.loan.LoanRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    BookCopyRepository bookCopyRepository;

    @Mock
    LoanRepository loanRepository;

    @InjectMocks
    BookService bookService;

    private Book sampleBook;
    private BookCopy sampleCopy;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setIsbn("978-4-000-00001-0");
        sampleBook.setTitle("吾輩は猫である");
        sampleBook.setAuthor("夏目漱石");
        sampleBook.setPublisher("○○出版");
        sampleBook.setPublishedYear(1905);
        sampleBook.setCallNumber("913.6");

        sampleCopy = new BookCopy();
        sampleCopy.setId(1L);
        sampleCopy.setBook(sampleBook);
        sampleCopy.setCopyCode("978-4-000-00001-0-001");
        sampleCopy.setStatus(CopyStatus.AVAILABLE);
    }

    // ─── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_noFilter_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.findAll(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("吾輩は猫である");
    }

    @Test
    void findAll_withKeyword_delegatesToSearchQuery() {
        when(bookRepository.findByTitleContainingOrAuthorContaining("猫", "猫"))
                .thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.findAll("猫", null);

        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_withCallNumber_delegatesToCallNumberQuery() {
        when(bookRepository.findByCallNumberStartingWith("913"))
                .thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.findAll(null, "913");

        assertThat(result).hasSize(1);
    }

    // ─── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsBookResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookResponse result = bookService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.isbn()).isEqualTo("978-4-000-00001-0");
    }

    @Test
    void findById_unknownId_throwsBookNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    // ─── create ────────────────────────────────────────────────────────────────

    @Test
    void create_validRequest_savesAndReturnsResponse() {
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookRequest request = new BookRequest(
                "978-4-000-00001-0", "吾輩は猫である", "夏目漱石", "○○出版", 1905, "913.6");

        BookResponse result = bookService.create(request);

        assertThat(result.title()).isEqualTo("吾輩は猫である");
        verify(bookRepository).save(any(Book.class));
    }

    // ─── update ────────────────────────────────────────────────────────────────

    @Test
    void update_existingId_updatesAndReturnsResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookRequest request = new BookRequest(
                "978-4-000-00001-0", "新タイトル", "夏目漱石", "○○出版", 1905, "913.6");

        BookResponse result = bookService.update(1L, request);

        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void update_unknownId_throwsBookNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BookRequest request = new BookRequest(
                "978-4-000-00001-0", "タイトル", "著者", "出版社", 2000, "000");

        assertThatThrownBy(() -> bookService.update(99L, request))
                .isInstanceOf(BookNotFoundException.class);
    }

    // ─── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_noCopies_deletesSuccessfully() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookCopyRepository.findByBookId(1L)).thenReturn(List.of());

        bookService.delete(1L);

        verify(bookRepository).delete(sampleBook);
    }

    @Test
    void delete_hasCopies_throwsBookHasCopiesException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookCopyRepository.findByBookId(1L)).thenReturn(List.of(sampleCopy));

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BookHasCopiesException.class);
    }

    @Test
    void delete_unknownId_throwsBookNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.delete(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    // ─── findCopies ────────────────────────────────────────────────────────────

    @Test
    void findCopies_existingBook_returnsCopyList() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookCopyRepository.findByBookId(1L)).thenReturn(List.of(sampleCopy));

        List<BookCopyResponse> result = bookService.findCopies(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(CopyStatus.AVAILABLE);
    }

    // ─── addCopy ───────────────────────────────────────────────────────────────

    @Test
    void addCopy_existingBook_savesAndReturnsCopyResponse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(sampleCopy);

        BookCopyRequest request = new BookCopyRequest("978-4-000-00001-0-001");

        BookCopyResponse result = bookService.addCopy(1L, request);

        assertThat(result.status()).isEqualTo(CopyStatus.AVAILABLE);
        verify(bookCopyRepository).save(any(BookCopy.class));
    }

    // ─── updateCopyStatus ──────────────────────────────────────────────────────

    @Test
    void updateCopyStatus_availableToRepair_updatesStatus() {
        sampleCopy.setStatus(CopyStatus.AVAILABLE);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(sampleCopy));
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(sampleCopy);

        BookCopyResponse result = bookService.updateCopyStatus(1L, 1L, CopyStatus.REPAIR);

        assertThat(result).isNotNull();
        verify(bookCopyRepository).save(any(BookCopy.class));
    }

    @Test
    void updateCopyStatus_checkedOutToAvailable_throwsIllegalStatusTransitionException() {
        sampleCopy.setStatus(CopyStatus.CHECKED_OUT);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(sampleCopy));

        assertThatThrownBy(() -> bookService.updateCopyStatus(1L, 1L, CopyStatus.AVAILABLE))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }

    // ─── deleteCopy ────────────────────────────────────────────────────────────

    @Test
    void deleteCopy_availableCopy_deletesSuccessfully() {
        sampleCopy.setStatus(CopyStatus.AVAILABLE);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(sampleCopy));

        bookService.deleteCopy(1L, 1L);

        verify(bookCopyRepository).delete(sampleCopy);
    }

    @Test
    void deleteCopy_checkedOutCopy_throwsCopyCheckedOutException() {
        sampleCopy.setStatus(CopyStatus.CHECKED_OUT);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(sampleCopy));

        assertThatThrownBy(() -> bookService.deleteCopy(1L, 1L))
                .isInstanceOf(CopyCheckedOutException.class);
    }
}
