package com.flashcard.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    private Deck testDeck;

    @BeforeEach
    void setUp() {
        testDeck = new Deck("Test Deck");
        testDeck.setId(1L);
    }

    @Test
    void defaultConstructor_ShouldSetCreatedAt() {
        // When
        Card card = new Card();

        // Then
        assertNotNull(card.getCreatedAt());
        assertTrue(card.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(card.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void twoParameterConstructor_ShouldSetQuestionAndAnswer() {
        // Given
        String question = "What is Java?";
        String answer = "A programming language";

        // When
        Card card = new Card(question, answer);

        // Then
        assertEquals(question, card.getQuestion());
        assertEquals(answer, card.getAnswer());
        assertNotNull(card.getCreatedAt());
        assertNull(card.getDeck());
    }

    @Test
    void threeParameterConstructor_ShouldSetAllFields() {
        // Given
        String question = "What is Java?";
        String answer = "A programming language";

        // When
        Card card = new Card(question, answer, testDeck);

        // Then
        assertEquals(question, card.getQuestion());
        assertEquals(answer, card.getAnswer());
        assertEquals(testDeck, card.getDeck());
        assertNotNull(card.getCreatedAt());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        Card card = new Card();
        Long id = 1L;
        String question = "Test Question";
        String answer = "Test Answer";
        LocalDateTime now = LocalDateTime.now();

        // When
        card.setId(id);
        card.setQuestion(question);
        card.setAnswer(answer);
        card.setCreatedAt(now);
        card.setDeck(testDeck);

        // Then
        assertEquals(id, card.getId());
        assertEquals(question, card.getQuestion());
        assertEquals(answer, card.getAnswer());
        assertEquals(now, card.getCreatedAt());
        assertEquals(testDeck, card.getDeck());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        // Given
        Card card = new Card("What is Java?", "A programming language");
        card.setId(1L);

        // When
        String result = card.toString();

        // Then
        assertEquals("Card{id=1, question='What is Java?', answer='A programming language'}", result);
    }

    @Test
    void toString_WithNullId_ShouldHandleGracefully() {
        // Given
        Card card = new Card("Question", "Answer");

        // When
        String result = card.toString();

        // Then
        assertEquals("Card{id=null, question='Question', answer='Answer'}", result);
    }
}
