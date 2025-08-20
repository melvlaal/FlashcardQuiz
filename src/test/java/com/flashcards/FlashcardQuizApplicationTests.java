package com.flashcards;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for the Flashcard Quiz application
 */
@SpringBootTest
@ActiveProfiles("test")
class FlashcardQuizApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}