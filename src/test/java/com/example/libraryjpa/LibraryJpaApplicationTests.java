package com.example.libraryjpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("its") // in-memory H2: do not depend on the dev database file in ./data
class LibraryJpaApplicationTests {

    @Test
    void contextLoads() {
    }
}
