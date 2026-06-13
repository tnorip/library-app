package training.aidd.library.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);

    List<Book> findByCallNumberStartingWith(String callNumber);

    @Query("SELECT b.callNumber FROM Book b")
    List<String> findAllCallNumbers();
}
