package training.aidd.library.book;

import java.util.List;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        Integer publishedYear,
        String callNumber,
        List<BookCopyResponse> copies
) {}
