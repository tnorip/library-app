package training.aidd.library.book;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public record BookCopyResponse(
        Long id,
        String copyCode,
        CopyStatus status,
        @Nullable LocalDate dueDate
) {}
