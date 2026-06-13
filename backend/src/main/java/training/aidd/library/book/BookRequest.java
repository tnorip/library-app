package training.aidd.library.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @Size(max = 20) String isbn,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 100) String author,
        @Size(max = 100) String publisher,
        @Min(1000) @Max(2100) Integer publishedYear,
        @Size(max = 20) String callNumber
) {}
