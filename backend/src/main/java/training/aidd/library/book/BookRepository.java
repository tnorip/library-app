package training.aidd.library.book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);

    List<Book> findByCallNumberStartingWith(String callNumber);
}
