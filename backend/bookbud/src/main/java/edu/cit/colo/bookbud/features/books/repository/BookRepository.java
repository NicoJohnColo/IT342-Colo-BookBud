package edu.cit.colo.bookbud.features.books.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.books.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    List<Book> findByOwnerUserId(String ownerId);

    Page<Book> findByStatus(Book.Status status, Pageable pageable);
}
