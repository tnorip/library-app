package training.aidd.library.book;

public record BookRequest(
        String isbn,
        String title,
        String author,
        String publisher,
        Integer publishedYear,
        String callNumber
) {}
