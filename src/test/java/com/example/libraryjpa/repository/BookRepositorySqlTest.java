package com.example.libraryjpa.repository;

import com.example.libraryjpa.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("its")
@Sql("/sql/books.sql")
class BookRepositorySqlTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void theScriptInsertsFourBooks() {
        assertEquals(4, bookRepository.count());
        assertEquals("Effective Java", bookRepository.findById(3L).orElseThrow().getTitle());
    }

    @Test
    void newBooksStartAtId100() {
        Book saved = bookRepository.save(new Book("Refactoring", "Martin Fowler", 2018));
        assertEquals(100L, saved.getId());
    }
}
