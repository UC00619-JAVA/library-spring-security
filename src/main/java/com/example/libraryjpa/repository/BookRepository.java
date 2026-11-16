package com.example.libraryjpa.repository;

import com.example.libraryjpa.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAuthor(String author);

    Page<Book> findByAuthor(String author, Pageable pageable);

    List<Book> findByTitleContainingIgnoreCase(String text);

    // books + categories in ONE query (solves the N+1)
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT b FROM Book b")
    List<Book> findAllWithCategories();

    // Spring Data follows the relation (automatic JOIN)
    List<Book> findByCategoriesName(String name);

    @Query("SELECT b FROM Book b WHERE b.available = true ORDER BY b.title")
    List<Book> findAvailableBooks();
}
