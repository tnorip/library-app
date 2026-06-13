package training.aidd.library.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> listBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String callNumber) {
        return bookService.findAll(q, callNumber);
    }

    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookRequest request) {
        return bookService.create(request);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }

    @GetMapping("/{bookId}/copies")
    public List<BookCopyResponse> listCopies(@PathVariable Long bookId) {
        return bookService.findCopies(bookId);
    }

    @PostMapping("/{bookId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public BookCopyResponse addCopy(@PathVariable Long bookId, @RequestBody BookCopyRequest request) {
        return bookService.addCopy(bookId, request);
    }

    @PatchMapping("/{bookId}/copies/{copyId}/status")
    public BookCopyResponse updateCopyStatus(
            @PathVariable Long bookId,
            @PathVariable Long copyId,
            @RequestBody CopyStatusRequest request) {
        return bookService.updateCopyStatus(bookId, copyId, request.status());
    }

    @DeleteMapping("/{bookId}/copies/{copyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCopy(@PathVariable Long bookId, @PathVariable Long copyId) {
        bookService.deleteCopy(bookId, copyId);
    }
}
