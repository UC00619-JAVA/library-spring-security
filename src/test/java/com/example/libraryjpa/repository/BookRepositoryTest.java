package com.example.libraryjpa.repository;

import com.example.libraryjpa.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; // Boot 3: org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest            // JPA + in-memory H2; every test rolls back at the end
@ActiveProfiles("its")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book headFirst;

    @BeforeEach
    void insertBooks() {
        bookRepository.deleteAll(); // start from an empty table (rolled back with the test)
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", 2008));
        bookRepository.save(new Book("The Clean Coder", "Robert C. Martin", 2011));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", 2018));
        headFirst = new Book("Head First Java", "Kathy Sierra", 2005);
        headFirst.setAvailable(false); // borrowed
        bookRepository.save(headFirst);
    }

    @Test
    void findsTitlesContainingJava() {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase("java");
        assertEquals(2, books.size());
    }

    @Test
    void findAvailableBooksSortedByTitle() {
        List<String> titles = bookRepository.findAvailableBooks().stream()
                .map(Book::getTitle)
                .toList();
        assertEquals(List.of("Clean Code", "Effective Java", "The Clean Coder"), titles);
    }

    @Test
    void saveGeneratesAnId() {
        Book saved = bookRepository.save(new Book("Refactoring", "Martin Fowler", 2018));
        assertNotNull(saved.getId());
        assertEquals(5, bookRepository.count());
    }

    @Test
    void deleteByIdRemovesTheBook() {
        bookRepository.deleteById(headFirst.getId());
        assertEquals(3, bookRepository.count());
        assertFalse(bookRepository.existsById(headFirst.getId()));
    }

    @Test
    void deleteAllLeavesNoBooks() {
        bookRepository.deleteAll();
        assertEquals(0, bookRepository.count());
        // the other tests still see 4 books: this one is rolled back at the end
    }
}
