package edu.cit.colo.bookbud.repository;

import edu.cit.colo.bookbud.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    List<Book> findByOwnerUserId(String ownerId);

    Page<Book> findByStatus(Book.Status status, Pageable pageable);
}
