package com.example.libraryjpa.controller;

import com.example.libraryjpa.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("its")
@Sql("/sql/books.sql")
class BookApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void listsTheBooksFromTheDatabase() throws Exception {
        mockMvc.perform(get("/api/books?sort=title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void returnsOneBookById() throws Exception {
        mockMvc.perform(get("/api/books/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    void returns404ForAnUnknownBook() throws Exception {
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found: 99"));
    }

    @Test
    void returnsOnlyTheAvailableBooks() throws Exception {
        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Head First Java is borrowed
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void createsABookAndSavesIt() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring",
                                 "author": "Martin Fowler",
                                 "publishedYear": 2018}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/books/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.available").value(true));

        // and in the database?
        assertEquals(5, bookRepository.count());
        assertTrue(bookRepository.existsById(100L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void doesNotSaveAnInvalidBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "author": "",
                                 "publishedYear": 1200}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields.title").value("Title is required"));

        assertEquals(4, bookRepository.count());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void updatesABook() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Clean Code (2nd Edition)",
                                 "author": "Robert C. Martin",
                                 "publishedYear": 2008}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code (2nd Edition)"));

        assertEquals("Clean Code (2nd Edition)",
                bookRepository.findById(1L).orElseThrow().getTitle());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void deletesABook() throws Exception {
        mockMvc.perform(delete("/api/books/4"))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.existsById(4L));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void deletingTwiceReturns404() throws Exception {
        mockMvc.perform(delete("/api/books/4"))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/books/4"))
                .andExpect(status().isNotFound());
    }

    // --- security ---

    @Test
    void creatingABookWithoutCredentialsReturns401() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring",
                                 "author": "Martin Fowler",
                                 "publishedYear": 2018}
                                """))
                .andExpect(status().isUnauthorized());

        assertEquals(4, bookRepository.count());
    }

    @Test
    void creatingABookWithAWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(httpBasic("admin", "wrong"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring",
                                 "author": "Martin Fowler",
                                 "publishedYear": 2018}
                                """))
                .andExpect(status().isUnauthorized());

        assertEquals(4, bookRepository.count());
    }

    @Test
    void deletingABookAsANormalUserReturns403() throws Exception {
        mockMvc.perform(delete("/api/books/4")
                        .with(httpBasic("ana", "ana123")))
                .andExpect(status().isForbidden());

        assertTrue(bookRepository.existsById(4L));
    }
}
