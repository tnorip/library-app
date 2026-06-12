package training.aidd.library.book;

public record BookCopyResponse(
        Long id,
        String copyCode,
        CopyStatus status
) {}
