package training.aidd.library.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(BookController.class)
@WithMockUser
class BookControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    BookService bookService;

    // ─── GET /api/books → 200 ─────────────────────────────────────────────────

    @Test
    void listBooks_noFilter_returns200() {
        when(bookService.findAll(isNull(), isNull())).thenReturn(List.of());

        assertThat(mockMvc.get().uri("/api/books"))
                .hasStatus(200);
    }

    @Test
    void listBooks_withKeyword_returns200() {
        when(bookService.findAll(eq("猫"), isNull())).thenReturn(List.of());

        assertThat(mockMvc.get().uri("/api/books").param("q", "猫"))
                .hasStatus(200);
    }

    // ─── GET /api/books/{id} → 200 / 404 ─────────────────────────────────────

    @Test
    void getBook_existingId_returns200() {
        BookResponse response = new BookResponse(
                1L, "978-4-000-00001-0", "吾輩は猫である", "夏目漱石", "○○出版", 1905, "913.6", List.of());
        when(bookService.findById(1L)).thenReturn(response);

        assertThat(mockMvc.get().uri("/api/books/1"))
                .hasStatus(200)
                .bodyJson().extractingPath("$.title").isEqualTo("吾輩は猫である");
    }

    @Test
    void getBook_unknownId_returns404() {
        when(bookService.findById(99L)).thenThrow(new BookNotFoundException(99L));

        assertThat(mockMvc.get().uri("/api/books/99"))
                .hasStatus(404);
    }

    // ─── POST /api/books → 201 ────────────────────────────────────────────────

    @Test
    void createBook_validRequest_returns201() {
        BookResponse response = new BookResponse(
                1L, "978-4-000-00001-0", "吾輩は猫である", "夏目漱石", "○○出版", 1905, "913.6", List.of());
        when(bookService.create(any(BookRequest.class))).thenReturn(response);

        assertThat(mockMvc.post().uri("/api/books")
                .contentType("application/json")
                .content("""
                        {
                          "isbn": "978-4-000-00001-0",
                          "title": "吾輩は猫である",
                          "author": "夏目漱石",
                          "publisher": "○○出版",
                          "publishedYear": 1905,
                          "callNumber": "913.6"
                        }
                        """))
                .hasStatus(201);
    }

    // ─── PUT /api/books/{id} → 200 / 404 ─────────────────────────────────────

    @Test
    void updateBook_existingId_returns200() {
        BookResponse response = new BookResponse(
                1L, "978-4-000-00001-0", "新タイトル", "夏目漱石", "○○出版", 1905, "913.6", List.of());
        when(bookService.update(eq(1L), any(BookRequest.class))).thenReturn(response);

        assertThat(mockMvc.put().uri("/api/books/1")
                .contentType("application/json")
                .content("""
                        {
                          "isbn": "978-4-000-00001-0",
                          "title": "新タイトル",
                          "author": "夏目漱石",
                          "publisher": "○○出版",
                          "publishedYear": 1905,
                          "callNumber": "913.6"
                        }
                        """))
                .hasStatus(200);
    }

    @Test
    void updateBook_unknownId_returns404() {
        when(bookService.update(eq(99L), any(BookRequest.class)))
                .thenThrow(new BookNotFoundException(99L));

        assertThat(mockMvc.put().uri("/api/books/99")
                .contentType("application/json")
                .content("""
                        {
                          "isbn": "978-4-000-00001-0",
                          "title": "タイトル",
                          "author": "著者",
                          "publisher": "出版社",
                          "publishedYear": 2000,
                          "callNumber": "000"
                        }
                        """))
                .hasStatus(404);
    }

    // ─── DELETE /api/books/{id} → 204 / 400 ──────────────────────────────────

    @Test
    void deleteBook_noCopies_returns204() {
        assertThat(mockMvc.delete().uri("/api/books/1"))
                .hasStatus(204);
    }

    @Test
    void deleteBook_hasCopies_returns400() {
        doThrow(new BookHasCopiesException(1L)).when(bookService).delete(1L);

        assertThat(mockMvc.delete().uri("/api/books/1"))
                .hasStatus(400);
    }

    // ─── GET /api/books/{bookId}/copies → 200 ────────────────────────────────

    @Test
    void listCopies_existingBook_returns200() {
        when(bookService.findCopies(1L)).thenReturn(List.of());

        assertThat(mockMvc.get().uri("/api/books/1/copies"))
                .hasStatus(200);
    }

    // ─── POST /api/books/{bookId}/copies → 201 ───────────────────────────────

    @Test
    void addCopy_validRequest_returns201() {
        BookCopyResponse response = new BookCopyResponse(1L, "978-4-000-00001-0-001", CopyStatus.AVAILABLE, null);
        when(bookService.addCopy(eq(1L), any(BookCopyRequest.class))).thenReturn(response);

        assertThat(mockMvc.post().uri("/api/books/1/copies")
                .contentType("application/json")
                .content("""
                        { "copyCode": "978-4-000-00001-0-001" }
                        """))
                .hasStatus(201);
    }

    // ─── PATCH /api/books/{bookId}/copies/{copyId}/status → 200 ─────────────

    @Test
    void updateCopyStatus_validTransition_returns200() {
        BookCopyResponse response = new BookCopyResponse(1L, "978-4-000-00001-0-001", CopyStatus.REPAIR, null);
        when(bookService.updateCopyStatus(1L, 1L, CopyStatus.REPAIR)).thenReturn(response);

        assertThat(mockMvc.patch().uri("/api/books/1/copies/1/status")
                .contentType("application/json")
                .content("""
                        { "status": "REPAIR" }
                        """))
                .hasStatus(200);
    }

    // ─── DELETE /api/books/{bookId}/copies/{copyId} → 204 / 409 ─────────────

    @Test
    void deleteCopy_availableCopy_returns204() {
        assertThat(mockMvc.delete().uri("/api/books/1/copies/1"))
                .hasStatus(204);
    }

    @Test
    void deleteCopy_checkedOutCopy_returns409() {
        doThrow(new CopyCheckedOutException(1L)).when(bookService).deleteCopy(1L, 1L);

        assertThat(mockMvc.delete().uri("/api/books/1/copies/1"))
                .hasStatus(409);
    }
}
