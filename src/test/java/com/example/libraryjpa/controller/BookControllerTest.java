package com.example.libraryjpa.controller;

import com.example.libraryjpa.config.SecurityConfig;
import com.example.libraryjpa.exception.BookNotFoundException;
import com.example.libraryjpa.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest; // Boot 3: org.springframework.boot.test.autoconfigure.web.servlet
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class) // @WebMvcTest does not load @Configuration classes: without this, Boot's default security applies (everything 401, CSRF on)
@ActiveProfiles("its")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // replaces the real BookService bean
    private BookService bookService;

    @Test
    void returns404WhenTheBookDoesNotExist() throws Exception {
        when(bookService.findById(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Book not found: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // POST /api/books requires ROLE_ADMIN
    void returns400WhenTheTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "author": "Martin Fowler", "publishedYear": 2018}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").value("Title is required"));

        verify(bookService, never()).create(any());
    }

    @Test
    void returns401WhenCreatingWithoutCredentials() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring", "author": "Martin Fowler", "publishedYear": 2018}
                                """))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void returns403WhenANormalUserCreatesABook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Refactoring", "author": "Martin Fowler", "publishedYear": 2018}
                                """))
                .andExpect(status().isForbidden());

        verify(bookService, never()).create(any());
    }
}
